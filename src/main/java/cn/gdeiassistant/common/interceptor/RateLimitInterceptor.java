package cn.gdeiassistant.common.interceptor;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.i18n.MessageCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import cn.gdeiassistant.common.i18n.Messages;

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

        // 清理其他已过期的 key，防止 map 无限增长，且通过 computeIfPresent 协调桶访问
        if (requestLog.size() > 1000) {
            for (String candidateKey : requestLog.keySet()) {
                if (key.equals(candidateKey)) {
                    continue;
                }
                requestLog.computeIfPresent(candidateKey, (k, q) -> {
                    Long head;
                    while ((head = q.peekFirst()) != null && now - head > windowMillis) {
                        q.pollFirst();
                    }
                    return q.isEmpty() ? null : q;
                });
            }
        }

        boolean[] allowed = new boolean[1];
        int[] currentCount = new int[1];
        requestLog.compute(key, (k, existing) -> {
            Deque<Long> timestamps = existing != null ? existing : new ConcurrentLinkedDeque<>();
            Long head;
            while ((head = timestamps.peekFirst()) != null && now - head > windowMillis) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= rateLimit.maxRequests()) {
                allowed[0] = false;
                currentCount[0] = timestamps.size();
            } else {
                timestamps.addLast(now);
                allowed[0] = true;
            }
            return timestamps;
        });

        if (!allowed[0]) {
            log.warn("请求限流: maxRequests={}, windowSeconds={}, currentRequests={}",
                    rateLimit.maxRequests(), rateLimit.windowSeconds(), currentCount[0]);
            String message = Messages.forLanguage("request.tooFrequent", request.getHeader("Accept-Language"));
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print("{\"success\":false,\"code\":429,\"message\":\"" + jsonEscape(message) + "\"}");
            response.getWriter().flush();
            return false;
        }

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
        // Only trust X-Forwarded-For when the immediate peer is a loopback or private proxy
        String remoteAddr = request.getRemoteAddr();
        if (isTrustedProxy(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isEmpty()) {
                String clientIp = forwarded.split(",")[0].trim();
                if (!clientIp.isEmpty()) {
                    return "ip:" + clientIp;
                }
            }
        }
        return "ip:" + remoteAddr;
    }

    private static boolean isTrustedProxy(String addr) {
        if (addr == null || addr.isEmpty()) {
            return false;
        }
        if ("127.0.0.1".equals(addr) || addr.startsWith("127.")
                || "::1".equals(addr) || "0:0:0:0:0:0:0:1".equals(addr)) {
            return true;
        }
        if (addr.startsWith("10.") || addr.startsWith("192.168.")) {
            return true;
        }
        if (addr.startsWith("172.")) {
            int secondDot = addr.indexOf('.', 4);
            if (secondDot > 4) {
                try {
                    int secondOctet = Integer.parseInt(addr.substring(4, secondDot));
                    return secondOctet >= 16 && secondOctet <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }
}
