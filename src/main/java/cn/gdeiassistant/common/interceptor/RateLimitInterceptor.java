package cn.gdeiassistant.common.interceptor;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 基于 IP 的滑动窗口限流拦截器。
 * 当方法标注 @RateLimit 时生效，超过阈值返回 429 Too Many Requests。
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        String rateLimitKey = getRateLimitKey(request);
        String path = request.getRequestURI();
        String key = rateLimitKey + ":" + path;

        long now = System.currentTimeMillis();
        long windowMillis = rateLimit.windowSeconds() * 1000L;

        Deque<Long> timestamps = requestLog.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // 清理窗口外的旧记录
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= rateLimit.maxRequests()) {
            log.warn("请求限流: key={}, path={}, 窗口内请求数={}", sanitizeLogParam(rateLimitKey), sanitizeLogParam(path), timestamps.size());
            String message = BackendTextLocalizer.localizeMessage("请求过于频繁，请稍后再试", request.getHeader("Accept-Language"));
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print("{\"code\":429,\"message\":\"" + jsonEscape(message) + "\"}");
            response.getWriter().flush();
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    private static String sanitizeLogParam(String value) {
        if (value == null) return "null";
        return value.replaceAll("[\\r\\n\\t]", "_");
    }

    private static String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String getRateLimitKey(HttpServletRequest request) {
        // For authenticated requests, use sessionId to prevent IP spoofing
        Object sessionId = request.getAttribute("sessionId");
        if (sessionId != null && !sessionId.toString().isEmpty()) {
            return "session:" + sessionId;
        }
        // For unauthenticated requests, use remote addr (not forwarded headers)
        return "ip:" + request.getRemoteAddr();
    }
}
