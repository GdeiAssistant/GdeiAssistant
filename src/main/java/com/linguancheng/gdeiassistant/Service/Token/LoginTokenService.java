package com.linguancheng.gdeiassistant.Service.Token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.linguancheng.gdeiassistant.Enum.Base.TokenValidResultEnum;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.TokenNotMatchingException;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.TokenServerException;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.UnusualLocationException;
import com.linguancheng.gdeiassistant.Pojo.Entity.*;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.TokenRefresh.TokenRefreshResult;
import com.linguancheng.gdeiassistant.Repository.Redis.LoginToken.LoginTokenDao;
import com.linguancheng.gdeiassistant.Service.IPAddress.IPService;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/10/28
 */

@Service
public class LoginTokenService {

    @Autowired
    private LoginTokenDao loginTokenDao;

    @Autowired
    private IPService ipService;

    @Value("#{propertiesReader['jwt.secret']}")
    private String secret;

    private Log log = LogFactory.getLog(LoginTokenService.class);

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
        TokenValidResultEnum validResultEnum = ValidToken(accessTokenSignature, ip);
        switch (validResultEnum) {
            case SUCCESS:
            case EXPIRED:
                //解析AccessToken
                Map<String, Claim> claimMap = ParseToken(accessTokenSignature);
                String username = claimMap.get("username").asString();
                String unionId = claimMap.get("unionId").asString();
                //从Redis缓存中移除令牌信息
                loginTokenDao.DeleteToken(accessTokenSignature);
                loginTokenDao.DeleteToken(refreshTokenSignature);
                //生成新的权限令牌和刷新令牌
                AccessToken accessToken = GetAccessToken(username, ip, unionId);
                RefreshToken refreshToken = GetRefreshToken(accessToken);
                //刷新令牌成功
                result.setAccessToken(accessToken);
                result.setRefreshToken(refreshToken);
                return result;

            case NOT_MATCH:
                throw new TokenNotMatchingException("令牌信息不匹配");

            case UNUSUAL_LOCATION:
                throw new UnusualLocationException("非常用地点登录");

            case ERROR:
            default:
                throw new TokenServerException("令牌服务系统异常");
        }
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
        String token = StringEncryptUtils.SHA256MapString(accessToken.getSignature());
        //生成RefreshToken
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setSignature(token);
        refreshToken.setTokenSignature(accessToken.getSignature());
        refreshToken.setCreateTime(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        refreshToken.setExpireTime(expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        refreshToken.setUnionId(accessToken.getUnionId());
        refreshToken.setIp(accessToken.getIp());
        if (loginTokenDao.SaveRefreshToken(refreshToken)) {
            return refreshToken;
        }
        throw new TokenServerException("令牌服务系统异常");
    }

    /**
     * 生成AccessToken
     *
     * @param username
     * @param ip
     * @param unionId
     * @return
     */
    public AccessToken GetAccessToken(String username, String ip, String unionId) throws Exception {
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime expireTime = createTime.plusDays(7);
        //生成AccessToken签名
        String token = JWT.create().withIssuer("gdeiassistant")
                .withClaim("username", username).withClaim("unionId", unionId)
                .withClaim("createTime"
                        , createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .withClaim("expireTime", expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .sign(Algorithm.HMAC256(secret));
        AccessToken accessToken = new AccessToken();
        accessToken.setUsername(username);
        accessToken.setIp(ip);
        accessToken.setUnionId(unionId);
        accessToken.setSignature(token);
        accessToken.setCreateTime(createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        accessToken.setExpireTime(expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        if (loginTokenDao.SaveAccessToken(accessToken)) {
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
    public TokenValidResultEnum ValidToken(String signature, String ip) {
        try {
            //验证Token是否被伪造或篡改
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).withIssuer("gdeiassistant").build();
            DecodedJWT decodedJWT = jwtVerifier.verify(signature);
            //获取时间戳，检验权限令牌是否已经过期
            Long expireTime = decodedJWT.getClaim("expireTime").asLong();
            if (expireTime <= LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) {
                //若过期时间为小于等于当前时间的时间戳，则认为权限令牌已过期
                return TokenValidResultEnum.EXPIRED;
            } else {
                //获取令牌信息记录的IP地址
                String tokenIP = loginTokenDao.QueryToken(signature);
                if (StringUtils.isBlank(tokenIP)) {
                    //若没有找到对应的令牌信息记录
                    return TokenValidResultEnum.NOT_MATCH;
                }
                //检测IP地址是否为同一省份
                Location currentLocation = ipService.GetLocationInfoByIPAddress(ip);
                Location tokenLocation = ipService.GetLocationInfoByIPAddress(tokenIP);
                if (currentLocation.getRegion().equals(tokenLocation.getRegion())) {
                    //IP校验通过
                    return TokenValidResultEnum.SUCCESS;
                } else {
                    return TokenValidResultEnum.UNUSUAL_LOCATION;
                }
            }
        } catch (JWTVerificationException e) {
            //签名验证不通过
            return TokenValidResultEnum.NOT_MATCH;
        } catch (Exception e) {
            log.error("校验客户端签名信息异常：", e);
            return TokenValidResultEnum.ERROR;
        }
    }
}
