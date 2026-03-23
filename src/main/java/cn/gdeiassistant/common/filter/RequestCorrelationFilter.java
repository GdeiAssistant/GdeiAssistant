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

/**
 * Assigns a unique request correlation ID to every inbound HTTP request and measures
 * wall-clock latency. The ID is propagated via:
 * <ul>
 *   <li>Request attribute {@code requestId}</li>
 *   <li>Response header {@code X-Request-ID}</li>
 *   <li>SLF4J MDC key {@code requestId}</li>
 * </ul>
 * If the caller already supplies an {@code X-Request-ID} header it is reused.
 */
@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        request.setAttribute("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);
        MDC.put("requestId", requestId);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            request.setAttribute("requestElapsedMs", elapsed);
            MDC.remove("requestId");
        }
    }
}
