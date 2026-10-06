package cn.gdeiassistant.common.filter;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.utils.JwtUtil;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtSessionIdFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserCertificateService userCertificateService;

    private JwtSessionIdFilter filter;

    @Mock private cn.gdeiassistant.core.user.mapper.UserMapper userMapper;

    @BeforeEach
    void setUp() {
        filter = new JwtSessionIdFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filter, "userCertificateService", userCertificateService);
        ReflectionTestUtils.setField(filter, "userMapper", userMapper);
        var active = new cn.gdeiassistant.core.user.pojo.entity.CampusAccountView();
        active.setStatus("ACTIVE");
        org.mockito.Mockito.lenient().when(userMapper.selectUser(org.mockito.ArgumentMatchers.anyString())).thenReturn(active);
    }

    @Test
    void shouldInjectSessionAndUserFromBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claim sessionIdClaim = mock(Claim.class);
        when(sessionIdClaim.isNull()).thenReturn(false);
        when(sessionIdClaim.asString()).thenReturn("session-1");
        when(jwtUtil.verifyAndParse("jwt-token")).thenReturn(Map.of("sessionId", sessionIdClaim));
        User user = new User("20240001");
        when(userCertificateService.getUserLoginCertificate("session-1")).thenReturn(user);

        filter.doFilter(request, response, chain);

        assertEquals("session-1", request.getAttribute("sessionId"));
        assertSame(user, request.getAttribute("user"));
        verify(chain).doFilter(request, response);
    }

    @Test
    void closedApplicationAccountRejectsCachedSessionBeforeCleanupCompletes() throws Exception {
        var request = new MockHttpServletRequest(); request.addHeader("Authorization", "Bearer cached-token");
        var response = new MockHttpServletResponse(); var chain = mock(FilterChain.class);
        var claim = mock(Claim.class); when(claim.asString()).thenReturn("cached-session");
        when(jwtUtil.verifyAndParse("cached-token")).thenReturn(Map.of("sessionId", claim));
        when(userCertificateService.getUserLoginCertificate("cached-session")).thenReturn(new User("synthetic"));
        var closed = new cn.gdeiassistant.core.user.pojo.entity.CampusAccountView(); closed.setStatus("CLOSED");
        when(userMapper.selectUser("synthetic")).thenReturn(closed);
        filter.doFilter(request, response, chain);
        assertNull(request.getAttribute("sessionId")); assertNull(request.getAttribute("user"));
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreLegacyTokenHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", "legacy-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(request.getAttribute("sessionId"));
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldNotInjectSessionIdWhenSessionDeletedFromRedis() throws Exception {
        // Simulate: JWT is valid but user has logged out (session cleared from Redis)
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-but-revoked-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claim sessionIdClaim = mock(Claim.class);
        when(sessionIdClaim.isNull()).thenReturn(false);
        when(sessionIdClaim.asString()).thenReturn("logged-out-session");
        when(jwtUtil.verifyAndParse("valid-but-revoked-token")).thenReturn(Map.of("sessionId", sessionIdClaim));
        // Session no longer exists in Redis after logout
        when(userCertificateService.getUserLoginCertificate("logged-out-session")).thenReturn(null);

        filter.doFilter(request, response, chain);

        // Neither sessionId nor user should be set — downstream interceptor will return 401
        assertNull(request.getAttribute("sessionId"));
        assertNull(request.getAttribute("user"));
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreInvalidJwtAndContinueFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer broken-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.verifyAndParse("broken-token")).thenThrow(new JWTVerificationException("invalid"));

        filter.doFilter(request, response, chain);

        assertNull(request.getAttribute("sessionId"));
        assertNull(request.getAttribute("user"));
        verify(chain).doFilter(request, response);
    }
}
