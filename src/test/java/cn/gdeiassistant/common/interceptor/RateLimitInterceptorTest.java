package cn.gdeiassistant.common.interceptor;

import cn.gdeiassistant.common.annotation.RateLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitInterceptorTest {

    private RateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RateLimitInterceptor();
    }

    private HandlerMethod mockHandlerWithRateLimit(int maxRequests, int windowSeconds) {
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.maxRequests()).thenReturn(maxRequests);
        when(rateLimit.windowSeconds()).thenReturn(windowSeconds);
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(rateLimit);
        return handlerMethod;
    }

    private HandlerMethod mockHandlerWithoutRateLimit() {
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(null);
        return handlerMethod;
    }

    @Test
    void shouldAllowRequestsWithinLimit() throws Exception {
        HandlerMethod handler = mockHandlerWithRateLimit(3, 60);

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            boolean allowed = interceptor.preHandle(request, response, handler);
            assertTrue(allowed, "Request " + (i + 1) + " should be allowed");
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void shouldReturn429WhenLimitExceeded() throws Exception {
        HandlerMethod handler = mockHandlerWithRateLimit(3, 60);

        // 先发 3 个请求用完配额
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            interceptor.preHandle(request, response, handler);
        }

        // 第 4 个请求应被拒绝
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(request, response, handler);

        assertFalse(allowed);
        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("请求过于频繁"));
    }

    @Test
    void shouldNotLimitEndpointsWithoutAnnotation() throws Exception {
        HandlerMethod handler = mockHandlerWithoutRateLimit();

        for (int i = 0; i < 100; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
            MockHttpServletResponse response = new MockHttpServletResponse();
            boolean allowed = interceptor.preHandle(request, response, handler);
            assertTrue(allowed);
        }
    }

    @Test
    void shouldNotLimitNonHandlerMethodHandler() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 传入一个普通 Object（非 HandlerMethod）
        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
    }

    @Test
    void shouldTrackDifferentIPsSeparately() throws Exception {
        HandlerMethod handler = mockHandlerWithRateLimit(2, 60);

        // IP-A 发 2 个请求
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            request.setRemoteAddr("192.168.1.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            interceptor.preHandle(request, response, handler);
        }

        // IP-A 第 3 个应被拒
        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("POST", "/api/auth/login");
        blockedRequest.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(blockedRequest, blockedResponse, handler));
        assertEquals(429, blockedResponse.getStatus());

        // IP-B 应仍然允许
        MockHttpServletRequest otherIpRequest = new MockHttpServletRequest("POST", "/api/auth/login");
        otherIpRequest.setRemoteAddr("192.168.1.2");
        MockHttpServletResponse otherIpResponse = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(otherIpRequest, otherIpResponse, handler));
    }

    @Test
    void shouldRespectXForwardedForHeader() throws Exception {
        HandlerMethod handler = mockHandlerWithRateLimit(1, 60);

        MockHttpServletRequest request1 = new MockHttpServletRequest("POST", "/api/auth/login");
        request1.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request1, response1, handler));

        // 同一个 X-Forwarded-For 的第 2 个请求应被拒
        MockHttpServletRequest request2 = new MockHttpServletRequest("POST", "/api/auth/login");
        request2.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.3");
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request2, response2, handler));
        assertEquals(429, response2.getStatus());
    }

    @Test
    void shouldTrackDifferentPathsSeparately() throws Exception {
        HandlerMethod handler = mockHandlerWithRateLimit(1, 60);

        // /api/auth/login 第 1 个
        MockHttpServletRequest loginReq = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse loginResp = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(loginReq, loginResp, handler));

        // /api/auth/login 第 2 个应被拒
        MockHttpServletRequest loginReq2 = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse loginResp2 = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(loginReq2, loginResp2, handler));

        // /api/upload/presignedUrl 应仍然允许（不同路径）
        MockHttpServletRequest uploadReq = new MockHttpServletRequest("GET", "/api/upload/presignedUrl");
        MockHttpServletResponse uploadResp = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(uploadReq, uploadResp, handler));
    }
}
