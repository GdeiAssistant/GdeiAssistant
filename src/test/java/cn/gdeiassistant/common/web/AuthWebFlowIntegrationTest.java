package cn.gdeiassistant.common.web;

import cn.gdeiassistant.common.filter.JwtSessionIdFilter;
import cn.gdeiassistant.common.interceptor.ApiAuthInterceptor;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.Utils.JwtUtil;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthWebFlowIntegrationTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserCertificateService userCertificateService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        JwtSessionIdFilter jwtSessionIdFilter = new JwtSessionIdFilter();
        ReflectionTestUtils.setField(jwtSessionIdFilter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(jwtSessionIdFilter, "userCertificateService", userCertificateService);

        ApiAuthInterceptor apiAuthInterceptor = new ApiAuthInterceptor(List.of("/api/auth"));
        mockMvc = MockMvcBuilders.standaloneSetup(new TestAuthController())
                .addFilters(jwtSessionIdFilter)
                .addInterceptors(apiAuthInterceptor)
                .build();
    }

    @Test
    void shouldAllowWhitelistedRouteWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void shouldBlockProtectedRouteWithoutToken() throws Exception {
        mockMvc.perform(get("/api/protected/session"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"code\":401,\"message\":\"Unauthorized\"}"));
    }

    @Test
    void shouldBlockCampusCredentialStatusWithoutToken() throws Exception {
        mockMvc.perform(get("/api/campus-credential/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"code\":401,\"message\":\"Unauthorized\"}"));
    }

    @Test
    void shouldAllowProtectedRouteWhenBearerTokenResolvesSession() throws Exception {
        Claim sessionIdClaim = mock(Claim.class);
        when(sessionIdClaim.isNull()).thenReturn(false);
        when(sessionIdClaim.asString()).thenReturn("session-1");
        when(jwtUtil.verifyAndParse("good-token")).thenReturn(Map.of("sessionId", sessionIdClaim));
        when(userCertificateService.getUserLoginCertificate("session-1")).thenReturn(new User("20240001"));

        mockMvc.perform(get("/api/protected/session")
                        .header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.username").value("20240001"));
    }

    @Test
    void shouldRejectLegacyTokenHeaderForProtectedRoute() throws Exception {
        mockMvc.perform(get("/api/protected/header")
                        .header("token", "legacy-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"code\":401,\"message\":\"Unauthorized\"}"));
    }

    @Test
    void shouldRejectProtectedRouteWhenSessionRevokedAfterLogout() throws Exception {
        // JWT signature is valid, but session was cleared from Redis (user logged out)
        Claim sessionIdClaim = mock(Claim.class);
        when(sessionIdClaim.isNull()).thenReturn(false);
        when(sessionIdClaim.asString()).thenReturn("revoked-session");
        when(jwtUtil.verifyAndParse("revoked-token")).thenReturn(Map.of("sessionId", sessionIdClaim));
        // Simulate logout: Redis returns null for this session
        when(userCertificateService.getUserLoginCertificate("revoked-session")).thenReturn(null);

        mockMvc.perform(get("/api/protected/session")
                        .header("Authorization", "Bearer revoked-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"code\":401,\"message\":\"Unauthorized\"}"));
    }

    @Test
    void shouldRejectProtectedRouteWhenJwtVerificationFails() throws Exception {
        when(jwtUtil.verifyAndParse("bad-token")).thenThrow(new JWTVerificationException("invalid"));

        mockMvc.perform(get("/api/protected/session")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"code\":401,\"message\":\"Unauthorized\"}"));
    }

    @RestController
    static class TestAuthController {

        @GetMapping("/api/auth/ping")
        Map<String, String> ping() {
            return Map.of("status", "ok");
        }

        @GetMapping("/api/protected/session")
        Map<String, String> session(HttpServletRequest request) {
            User user = (User) request.getAttribute("user");
            return Map.of(
                    "sessionId", String.valueOf(request.getAttribute("sessionId")),
                    "username", user != null ? user.getUsername() : ""
            );
        }

        @GetMapping("/api/protected/header")
        String tokenHeader(@RequestHeader(value = "token", required = false) String token,
                           HttpServletRequest request) {
            return token != null ? String.valueOf(request.getAttribute("sessionId")) : "";
        }

        @GetMapping("/api/campus-credential/status")
        Map<String, String> campusCredentialStatus() {
            return Map.of("status", "ok");
        }
    }
}
