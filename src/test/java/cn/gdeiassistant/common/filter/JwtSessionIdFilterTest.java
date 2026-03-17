package cn.gdeiassistant.common.filter;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.redis.LoginToken.LoginTokenDao;
import cn.gdeiassistant.common.tools.Utils.JwtUtil;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtSessionIdFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private LoginTokenDao loginTokenDao;

    private JwtSessionIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtSessionIdFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filter, "userCertificateService", userCertificateService);
        ReflectionTestUtils.setField(filter, "loginTokenDao", loginTokenDao);
    }

    @Test
    void shouldInjectSessionAndUserFromBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claim tokenClaim = mock(Claim.class);
        when(tokenClaim.isNull()).thenReturn(false);
        when(tokenClaim.asString()).thenReturn("login-token");
        when(jwtUtil.verifyAndParse("jwt-token")).thenReturn(Map.of("token", tokenClaim));
        when(loginTokenDao.QuerySessionIdByWebToken("login-token")).thenReturn("session-1");
        User user = new User("20240001");
        when(userCertificateService.getUserLoginCertificate("session-1")).thenReturn(user);

        filter.doFilter(request, response, chain);

        assertEquals("session-1", request.getAttribute("sessionId"));
        assertSame(user, request.getAttribute("user"));
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldResolveMobileJwtWithSessionIdClaim() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer mobile-jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claim sessionIdClaim = mock(Claim.class);
        when(sessionIdClaim.isNull()).thenReturn(false);
        when(sessionIdClaim.asString()).thenReturn("session-2");
        when(jwtUtil.verifyAndParse("mobile-jwt-token")).thenReturn(Map.of("sessionId", sessionIdClaim));

        filter.doFilter(request, response, chain);

        assertEquals("session-2", request.getAttribute("sessionId"));
        verify(loginTokenDao, never()).QuerySessionIdByWebToken("mobile-jwt-token");
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
