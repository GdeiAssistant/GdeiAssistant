package cn.gdeiassistant.common.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Assigns a unique request correlation ID to every inbound HTTP request and measures
 * wall-clock latency. The ID is propagated via:
 * <ul>
 *   <li>Request attribute {@code requestId}</li>
 *   <li>Response header {@code X-Request-ID}</li>
 *   <li>SLF4J MDC key {@code requestId}</li>
 * </ul>
 * A bounded ASCII {@code X-Request-ID} is reused; malformed values are replaced.
 */
@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final Pattern REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || !REQUEST_ID.matcher(requestId).matches()) {
            requestId = UUID.randomUUID().toString();
        }

        request.setAttribute("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);
        String previousRequestId = MDC.get("requestId");
        MDC.put("requestId", requestId);

        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            request.setAttribute("requestElapsedMs", elapsed);
            if (previousRequestId == null) {
                MDC.remove("requestId");
            } else {
                MDC.put("requestId", previousRequestId);
            }
        }
    }
}
