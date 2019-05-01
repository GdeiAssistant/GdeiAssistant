package edu.gdei.gdeiassistant.Service.Token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import edu.gdei.gdeiassistant.Exception.TokenValidException.TokenExpiredException;
import edu.gdei.gdeiassistant.Exception.TokenValidException.TokenNotMatchingException;
import edu.gdei.gdeiassistant.Exception.TokenValidException.TokenServerException;
import edu.gdei.gdeiassistant.Exception.TokenValidException.UnusualLocationException;
import edu.gdei.gdeiassistant.Pojo.Entity.AccessToken;
import edu.gdei.gdeiassistant.Pojo.Entity.Location;
import edu.gdei.gdeiassistant.Pojo.Entity.RefreshToken;
import edu.gdei.gdeiassistant.Pojo.TokenRefresh.TokenRefreshResult;
import edu.gdei.gdeiassistant.Repository.Redis.LoginToken.LoginTokenDao;
import edu.gdei.gdeiassistant.Service.IPAddress.IPService;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class LoginTokenService {

    @Autowired
    private LoginTokenDao loginTokenDao;

    @Autowired
    private IPService ipService;

    @Value("#{propertiesReader['jwt.secret']}")
    private String secret;

    /**
     * 使令牌过期，从缓存中过期
     *
     * @param signature
     * @return
     */
    public void ExpireToken(String signature) throws Exception {
        if (StringUtils.isBlank(loginTokenDao.QueryToken(signature))) {
            throw new TokenNotMatchingException("令牌信息不存在");
        }
        if (!loginTokenDao.DeleteToken(signature)) {
            throw new TokenServerException("令牌服务系统异常");
        }
    }

    /**
     * 刷新权限令牌，并生成一个新的刷新令牌
     *
     * @param refreshTokenSignature
     * @param ip
     * @return
     */
    public TokenRefreshResult RefreshToken(String refreshTokenSignature, String ip) throws Exception {
        TokenRefreshResult result = new TokenRefreshResult();
        //从Redis缓存中查找权限令牌签名
        String accessTokenSignature = loginTokenDao
                .QueryToken(refreshTokenSignature);
        if (StringUtils.isBlank(accessTokenSignature)) {
            //没有对应的权限令牌
            throw new TokenNotMatchingException("没有对应的权限令牌");
        }
        //校验AccessToken信息
        try {
            ValidToken(accessTokenSignature, ip);
        } catch (TokenExpiredException ignored) {
            //忽略Token令牌是否过期
        }
        //解析AccessToken
        Map<String, Claim> claimMap = ParseToken(accessTokenSignature);
        String username = claimMap.get("username").asString();
        String unionid = claimMap.get("unionid").asString();
        //从Redis缓存中移除令牌信息
        loginTokenDao.DeleteToken(accessTokenSignature);
        loginTokenDao.DeleteToken(refreshTokenSignature);
        //生成新的权限令牌和刷新令牌
        AccessToken accessToken = GetAccessToken(username, ip, unionid);
        RefreshToken refreshToken = GetRefreshToken(accessToken);
        //刷新令牌成功
        result.setAccessToken(accessToken);
        result.setRefreshToken(refreshToken);
        return result;
    }

    /**
     * 生成RefreshToken
     *
     * @param accessToken
     * @return
     */
    public RefreshToken GetRefreshToken(AccessToken accessToken) throws Exception {
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime expireTime = createTime.plusDays(30);
        //生成RefreshToken签名
        String token = StringEncryptUtils.SHA256HexString(accessToken.getSignature());
        //生成RefreshToken
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setSignature(token);
        refreshToken.setTokenSignature(accessToken.getSignature());
        refreshToken.setCreateTime(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        refreshToken.setExpireTime(expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        refreshToken.setUnionid(accessToken.getUnionid());
        refreshToken.setIp(accessToken.getIp());
        if (loginTokenDao.InsertRefreshToken(refreshToken)) {
            return refreshToken;
        }
        throw new TokenServerException("令牌服务系统异常");
    }

    /**
     * 生成AccessToken
     *
     * @param username
     * @param ip
     * @param unionid
     * @return
     */
    public AccessToken GetAccessToken(String username, String ip, String unionid) throws Exception {
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime expireTime = createTime.plusDays(7);
        //生成AccessToken签名
        String token = JWT.create().withIssuer("gdeiassistant")
                .withClaim("username", username).withClaim("unionid", unionid == null
                        ? UUID.randomUUID().toString() : unionid)
                .withClaim("createTime"
                        , createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .withClaim("expireTime", expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .sign(Algorithm.HMAC256(secret));
        AccessToken accessToken = new AccessToken();
        accessToken.setUsername(username);
        accessToken.setIp(ip);
        accessToken.setUnionid(unionid);
        accessToken.setSignature(token);
        accessToken.setCreateTime(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        accessToken.setExpireTime(expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        if (loginTokenDao.InsertAccessToken(accessToken)) {
            return accessToken;
        }
        throw new TokenServerException("令牌服务系统异常");
    }

    /**
     * 解析令牌信息
     *
     * @param signature
     * @return
     */
    public Map<String, Claim> ParseToken(String signature) {
        return JWT.decode(signature).getClaims();
    }

    /**
     * 校验客户端提交的签名信息
     *
     * @param signature
     * @param ip
     * @return
     */
    public void ValidToken(String signature, String ip) throws TokenNotMatchingException, UnusualLocationException, TokenServerException, TokenExpiredException {
        try {
            //验证Token是否被伪造或篡改
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).withIssuer("gdeiassistant").build();
            DecodedJWT decodedJWT = jwtVerifier.verify(signature);
            //获取时间戳，检验权限令牌是否已经过期
            Long expireTime = decodedJWT.getClaim("expireTime").asLong();
            if (expireTime <= LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) {
                //若过期时间为小于等于当前时间的时间戳，则认为权限令牌已过期
                throw new TokenExpiredException("令牌过期异常");
            }
            //获取令牌信息记录的IP地址
            String tokenIP = loginTokenDao.QueryToken(signature);
            if (StringUtils.isBlank(tokenIP)) {
                //若没有找到对应的令牌信息记录
                throw new TokenNotMatchingException("没有找到对应的令牌信息记录");
            }
            //若IP地址相同，则不需要重复校验
            if (tokenIP.equals(ip)) {
                return;
            }
            //检测IP地址是否为同一省份
            Location currentLocation = ipService.GetLocationInfoByIPAddress(ip);
            Location tokenLocation = ipService.GetLocationInfoByIPAddress(tokenIP);
            if (Objects.equals(currentLocation.getCountry(), tokenLocation.getCountry())
                    && Objects.equals(currentLocation.getRegion(), tokenLocation.getRegion())) {
                //IP校验通过，更新令牌的IP地址
                AccessToken accessToken = new AccessToken();
                accessToken.setSignature(signature);
                accessToken.setIp(ip);
                if (loginTokenDao.UpdateAccessToken(accessToken)) {
                    return;
                }
                throw new TokenServerException("更新权限令牌异常");
            }
            throw new UnusualLocationException("用户在异常登录地使用");
        } catch (JWTVerificationException e) {
            //签名验证不通过
            throw new TokenNotMatchingException("签名验证不通过");
        }
    }
}
