package cn.gdeiassistant.common.filter;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.Utils.JwtUtil;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 当请求头带有 Authorization: Bearer <JWT> 时，校验 JWT 签名并从 claims 中提取 sessionId，
 * 将 sessionId 和 user 注入 request attribute，供后续拦截器与业务使用。
 */
@Component("jwtSessionIdFilter")
public class JwtSessionIdFilter implements Filter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserCertificateService userCertificateService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String jwtString = null;
        String auth = req.getHeader(AUTHORIZATION);
        if (auth != null && !auth.isEmpty()) {
            auth = auth.trim();
            if (auth.toLowerCase().startsWith(BEARER_PREFIX)) {
                jwtString = auth.substring(BEARER_PREFIX.length()).trim();
            }
        }
        if (jwtString != null && !jwtString.isEmpty()) {
            try {
                Map<String, Claim> claims = jwtUtil.verifyAndParse(jwtString);
                Claim sessionIdClaim = claims.get("sessionId");
                if (sessionIdClaim != null && !sessionIdClaim.isNull()) {
                    String sessionId = sessionIdClaim.asString();
                    if (sessionId != null && !sessionId.isEmpty()) {
                        // 验证 session 在 Redis 中仍然存在（用户未注销）
                        // 若 session 已被 clearUserLoginAndSession 清除，则不注入 sessionId，
                        // 后续 ApiAuthInterceptor 会返回 401
                        User certificate = userCertificateService.getUserLoginCertificate(sessionId);
                        if (certificate != null) {
                            req.setAttribute("sessionId", sessionId);
                            req.setAttribute("user", certificate);
                        }
                    }
                }
            } catch (JWTVerificationException ignored) {
                // 无效或过期的 JWT：不注入 sessionId，后续由 ApiAuthInterceptor 统一返回 401
            }
        }
        chain.doFilter(req, response);
    }
}
