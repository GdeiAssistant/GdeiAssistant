package cn.gdeiassistant.common.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 无状态 API 鉴权：仅根据 request 中的 sessionId attribute（由 JWT Filter 注入）判断是否已认证。
 * 不依赖 HttpSession，适用于 Vue3 + REST API 架构。
 */
public class ApiAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiAuthInterceptor.class);
    private final List<String> exceptionList;

    public ApiAuthInterceptor(List<String> exceptionList) {
        this.exceptionList = exceptionList;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath))
                ? uri.substring(contextPath.length()) : uri;
        if (path.isEmpty()) {
            path = "/";
        }

        for (String prefix : exceptionList) {
            if (path.startsWith(prefix) || path.equals("/")) {
                return true;
            }
        }

        // 仅对 /api 请求做鉴权：无 sessionId 则 401
        if (path.startsWith("/api")) {
            String sessionId = (String) request.getAttribute("sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                log.error("401拦截原因: 请求中无 sessionId（可能 Authorization 头为空或格式不对、JWT 解析或验签失败、Redis 中无对应 Session） path={}", path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print("{\"code\":401,\"message\":\"Unauthorized\"}");
                response.getWriter().flush();
                response.getWriter().close();
                return false;
            }
        }

        return true;
    }
}
