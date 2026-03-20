package cn.gdeiassistant.common.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAuthInterceptorTest {

    private final ApiAuthInterceptor interceptor = new ApiAuthInterceptor(List.of("/api/auth", "/about"));

    @Test
    void shouldAllowOptionsRequestsWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/secret/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldAllowWhitelistedPathsWithoutSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
    }

    @Test
    void shouldRejectProtectedApiRequestsWithoutSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secret/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
        assertEquals("{\"code\":401,\"message\":\"Unauthorized\"}", response.getContentAsString());
    }

    @Test
    void shouldAllowProtectedApiRequestsWhenSessionIdExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secret/profile");
        request.setAttribute("sessionId", "session-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals(200, response.getStatus());
    }
}
