package cn.gdeiassistant.Service.AccountManagement.Token;

import cn.gdeiassistant.Exception.TokenValidException.SuspiciouseRequestException;
import cn.gdeiassistant.Exception.TokenValidException.TokenExpiredException;
import cn.gdeiassistant.Exception.TokenValidException.TokenNotMatchingException;
import cn.gdeiassistant.Pojo.Config.JWTConfig;
import cn.gdeiassistant.Pojo.Entity.AccessToken;
import cn.gdeiassistant.Pojo.Entity.Device;
import cn.gdeiassistant.Pojo.Entity.Location;
import cn.gdeiassistant.Pojo.Entity.RefreshToken;
import cn.gdeiassistant.Pojo.TokenRefresh.TokenRefreshResult;
import cn.gdeiassistant.Repository.Redis.LoginToken.LoginTokenDao;
import cn.gdeiassistant.Service.OpenAPI.IPAddress.IPAddressService;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;

@Service
public class LoginTokenService {

    @Autowired
    private LoginTokenDao loginTokenDao;

    @Autowired
    private IPAddressService ipAddressService;

    @Autowired
    private JWTConfig jwtConfig;

    /**
     * 使令牌过期，从缓存中过期
     *
     * @param signature
     * @return
     */
    public void ExpireToken(String signature) throws Exception {
        AccessToken accessToken = loginTokenDao.QueryAccessToken(signature);
        if (accessToken == null) {
            throw new TokenNotMatchingException("令牌信息不存在");
        }
        loginTokenDao.DeleteAccessToken(signature);
        loginTokenDao.DeleteRefreshToken(StringEncryptUtils.SHA256HexString(accessToken.getSignature()));
    }

    /**
     * 刷新权限令牌，并生成一个新的刷新令牌
     *
     * @param refreshTokenSignature
     * @return
     */
    public TokenRefreshResult RefreshToken(String sessionId, String refreshTokenSignature) throws Exception {
        TokenRefreshResult result = new TokenRefreshResult();
        //从Redis缓存中查找权限令牌签名
        RefreshToken token = loginTokenDao.QueryRefreshToken(refreshTokenSignature);
        if (token == null) {
            //没有对应的权限令牌
            throw new TokenNotMatchingException("没有对应的权限令牌");
        }
        //校验AccessToken信息
        try {
            ValidToken(token.getAccessTokenSignature());
        } catch (TokenExpiredException ignored) {
            //忽略Token令牌是否过期
        }
        //解析AccessToken
        Map<String, Claim> claimMap = ParseToken(token.getAccessTokenSignature());
        String username = claimMap.get("username").asString();
        //从Redis缓存中移除令牌信息
        loginTokenDao.DeleteAccessToken(token.getAccessTokenSignature());
        loginTokenDao.DeleteRefreshToken(refreshTokenSignature);
        //生成新的权限令牌和刷新令牌
        AccessToken accessToken = GetAccessToken(username, sessionId);
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
    public RefreshToken GetRefreshToken(AccessToken accessToken) {
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime expireTime = createTime.plusDays(30);
        //生成RefreshToken签名
        String token = StringEncryptUtils.SHA256HexString(accessToken.getSignature());
        //生成RefreshToken
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setSignature(token);
        refreshToken.setAccessTokenSignature(accessToken.getSignature());
        refreshToken.setCreateTime(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        refreshToken.setExpireTime(expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        loginTokenDao.InsertRefreshToken(refreshToken);
        return refreshToken;
    }

    /**
     * 生成AccessToken
     *
     * @param sessionId
     * @param username
     * @return
     */
    public AccessToken GetAccessToken(String sessionId, String username) {
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime expireTime = createTime.plusDays(7);
        //生成AccessToken签名
        String token = JWT.create().withIssuer("gdeiassistant")
                .withClaim("username", username)
                .withClaim("sessionId", sessionId)
                .withClaim("createTime", createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .withClaim("expireTime", expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .sign(Algorithm.HMAC256(jwtConfig.getSecret()));
        AccessToken accessToken = new AccessToken();
        accessToken.setUsername(username);
        accessToken.setSignature(token);
        accessToken.setCreateTime(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        accessToken.setExpireTime(expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        loginTokenDao.InsertAccessToken(accessToken);
        return accessToken;
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
     * 保存设备信息
     *
     * @param signature
     * @param device
     */
    public void SaveDevice(String signature, Device device) {
        loginTokenDao.SaveDeviceData(signature, device);
    }

    /**
     * 校验客户端设备信息
     *
     * @param signature
     * @param ip
     * @param device
     */
    public void ValidDevice(String signature, String ip, Device device) throws TokenExpiredException, SuspiciouseRequestException {
        //获取上次使用令牌访问时的设备信息
        Device data = loginTokenDao.QueryDeviceData(signature);
        if (data == null) {
            //没有找到对应的设备信息记录
            throw new TokenExpiredException("令牌已过期");
        }
        if (!data.getUnionID().equals(device.getUnionID())) {
            //设备UnionID不匹配，校验IP地址
            if (data.getIP().equals(ip)) {
                //若IP地址相同，则不需要重复校验
                return;
            }
            //检测IP地址是否为同一省份
            Location currentLocation = ipAddressService.GetLocationInfoByIPAddress(ip);
            Location tokenLocation = ipAddressService.GetLocationInfoByIPAddress(device.getIP());
            if (Objects.equals(currentLocation.getCountry(), tokenLocation.getCountry())
                    && Objects.equals(currentLocation.getProvince(), tokenLocation.getProvince())) {
                //IP地址为同一省份，校验通过
                return;
            }
            //IP地址非同一省份，令牌失效
            loginTokenDao.DeleteAccessToken(signature);
            loginTokenDao.DeleteRefreshToken(StringEncryptUtils.SHA256HexString(signature));
            throw new SuspiciouseRequestException("可疑的登录请求");
        }
        //设备UnionID相同，校验通过
    }

    /**
     * 校验客户端提交的签名信息
     *
     * @param signature
     * @return
     */
    public void ValidToken(String signature) throws TokenNotMatchingException, TokenExpiredException {
        try {
            //验证Token是否被伪造或篡改
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret())).withIssuer("gdeiassistant").build();
            DecodedJWT decodedJWT = jwtVerifier.verify(signature);
            //获取时间戳，检验权限令牌是否已经过期
            Long expireTime = decodedJWT.getClaim("expireTime").asLong();
            if (expireTime <= LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) {
                //若过期时间为小于等于当前时间的时间戳，则认为权限令牌已过期
                throw new TokenExpiredException("令牌过期异常");
            }
            //校验通过
        } catch (JWTVerificationException e) {
            //签名验证不通过
            throw new TokenNotMatchingException("签名验证不通过");
        }
    }

    /**
     * 验证令牌有效期
     *
     * @param token
     */
    public void ValidExpiration(String token) throws TokenExpiredException {
        AccessToken accessToken = loginTokenDao.QueryAccessToken(token);
        if (accessToken != null) {
            Device device = loginTokenDao.QueryDeviceData(token);
            if (device != null) {
                return;
            }
        }
        throw new TokenExpiredException("令牌已过期");
    }

}