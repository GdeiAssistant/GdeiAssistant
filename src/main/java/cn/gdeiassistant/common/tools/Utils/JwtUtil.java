package cn.gdeiassistant.common.tools.Utils;

import cn.gdeiassistant.common.pojo.Config.JWTConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * 无状态 JWT 工具类，用于前后端分离场景下的登录鉴权。
 * Token 载荷包含 sessionId、username（学号）、过期时间等。
 */
@Component
public class JwtUtil {

    private static final String ISSUER = "gdeiassistant";
    private static final int EXPIRE_DAYS = 7;

    @Autowired
    private JWTConfig jwtConfig;

    /**
     * 为指定用户签发 JWT（载荷含 sessionId，供移动端或兼容场景使用）
     *
     * @param sessionId 会话标识（与 Redis 中用户凭证对应）
     * @param username  用户唯一标识（学号/校园网账号）
     * @return JWT 字符串
     */
    public String createToken(String sessionId, String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(EXPIRE_DAYS);
        return JWT.create()
                .withIssuer(ISSUER)
                .withClaim("username", username)
                .withClaim("sessionId", sessionId)
                .withClaim("createTime", now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .withClaim("expireTime", expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .sign(Algorithm.HMAC256(jwtConfig.getSecret()));
    }

    /**
     * 为 Web 登录签发“可撤销” JWT：载荷仅含 loginToken 与 username，sessionId 由 Redis token->sessionId 解析。
     *
     * @param loginToken 唯一 token，已写入 Redis（token -> sessionId）
     * @param username   用户唯一标识（学号/校园网账号）
     * @return JWT 字符串
     */
    public String createTokenWithLoginToken(String loginToken, String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(EXPIRE_DAYS);
        return JWT.create()
                .withIssuer(ISSUER)
                .withClaim("username", username)
                .withClaim("token", loginToken)
                .withClaim("createTime", now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .withClaim("expireTime", expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .sign(Algorithm.HMAC256(jwtConfig.getSecret()));
    }

    /**
     * 校验并解析 JWT，校验签名与过期时间
     *
     * @param token JWT 字符串
     * @return 载荷中的 claims，包含 username、sessionId 等
     * @throws JWTVerificationException 签名无效或已过期
     */
    public Map<String, Claim> verifyAndParse(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret()))
                .withIssuer(ISSUER)
                .build();
        DecodedJWT decoded = verifier.verify(token);
        long expireTime = decoded.getClaim("expireTime").asLong();
        if (expireTime <= System.currentTimeMillis()) {
            throw new JWTVerificationException("Token has expired");
        }
        return decoded.getClaims();
    }

    /**
     * 仅解析 JWT 载荷（不校验签名与过期），用于已由其他逻辑校验过的场景
     */
    public Map<String, Claim> parseToken(String token) {
        return JWT.decode(token).getClaims();
    }
}
