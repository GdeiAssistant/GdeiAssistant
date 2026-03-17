package cn.gdeiassistant.common.filter;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.redis.LoginToken.LoginTokenDao;
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
 * 当请求头带有 Authorization: Bearer &lt;JWT&gt; 时，校验 JWT 并将 sessionId、user 注入请求 attribute（无状态，不依赖 HttpSession）。
 * 逻辑：校验 JWT 签名与过期 → 若 payload 含 "token" 则用该 token 查 Redis 取 sessionId（无则拦截）；
 * 若仅含 "sessionId"（移动端兼容）则直接使用。再将 sessionId、user 写入 request.setAttribute，供 ApiAuthInterceptor 与业务使用。
 */
@Component("jwtSessionIdFilter")
public class JwtSessionIdFilter implements Filter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private LoginTokenDao loginTokenDao;

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
                String sessionId = resolveSessionId(claims);
                if (sessionId != null && !sessionId.isEmpty()) {
                    req.setAttribute("sessionId", sessionId);
                    User certificate = userCertificateService.getUserLoginCertificate(sessionId);
                    if (certificate != null) {
                        req.setAttribute("user", certificate);
                    }
                }
            } catch (JWTVerificationException ignored) {
                // 无效或过期的 JWT：不注入 sessionId，后续由 ApiAuthInterceptor 统一返回 401
            }
        }
        chain.doFilter(req, response);
    }

    /**
     * 从 JWT payload 解析 sessionId：优先 token -> Redis；兼容仅含 sessionId 的移动端 JWT。
     */
    private String resolveSessionId(Map<String, Claim> claims) {
        Claim tokenClaim = claims.get("token");
        if (tokenClaim != null && !tokenClaim.isNull()) {
            String loginToken = tokenClaim.asString();
            String sessionId = loginTokenDao.QuerySessionIdByWebToken(loginToken);
            return sessionId;
        }
        Claim sessionIdClaim = claims.get("sessionId");
        if (sessionIdClaim != null && !sessionIdClaim.isNull()) {
            return sessionIdClaim.asString();
        }
        return null;
    }
}
