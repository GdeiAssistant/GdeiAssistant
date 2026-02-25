package cn.gdeiassistant.common.redis.LoginToken;

import cn.gdeiassistant.common.pojo.Entity.AccessToken;
import cn.gdeiassistant.common.pojo.Entity.Device;
import cn.gdeiassistant.common.pojo.Entity.RefreshToken;
import cn.gdeiassistant.common.tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class LoginTokenDaoImpl implements LoginTokenDao {

    private final String ACCESS_TOKEN_PREFIX = "ACCESS_TOKEN_";

    private final String REFRESH_TOKEN_PREFIX = "REFRESH_TOKEN_";

    private final String DEVICE_DATA_PREFIX = "DEVICE_DATA_";

    private final String WEB_LOGIN_TOKEN_PREFIX = "WEB_LOGIN_TOKEN_";

    private final String WEB_SESSION_TO_TOKEN_PREFIX = "WEB_SESSION_TO_TOKEN_";

    private final int WEB_TOKEN_EXPIRE_DAYS = 7;

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 查找令牌签名对应的记录信息（Value 存 JSON 字符串，与 redisTemplate 的 StringRedisSerializer 一致）
     */
    @Override
    public AccessToken QueryAccessToken(String signature) {
        String key = StringEncryptUtils.sha256HexString(ACCESS_TOKEN_PREFIX + signature);
        String json = redisDaoUtils.get(key);
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, AccessToken.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis AccessToken 反序列化失败", e);
        }
    }

    /**
     * 查找刷新令牌对应信息（Value 存 accessTokenSignature 字符串）
     */
    @Override
    public RefreshToken QueryRefreshToken(String signature) {
        String key = StringEncryptUtils.sha256HexString(REFRESH_TOKEN_PREFIX + signature);
        String accessTokenSignature = redisDaoUtils.get(key);
        if (accessTokenSignature == null || accessTokenSignature.isEmpty()) return null;
        RefreshToken r = new RefreshToken();
        r.setSignature(signature);
        r.setAccessTokenSignature(accessTokenSignature);
        return r;
    }

    /**
     * 插入权限令牌信息（存 JSON 字符串）
     */
    @Override
    public void InsertAccessToken(AccessToken token) {
        String key = StringEncryptUtils.sha256HexString(ACCESS_TOKEN_PREFIX + token.getSignature());
        try {
            redisDaoUtils.set(key, objectMapper.writeValueAsString(token));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AccessToken 序列化失败", e);
        }
        redisDaoUtils.expire(key, 7, TimeUnit.DAYS);
    }

    /**
     * 插入刷新令牌信息（Value 存 accessTokenSignature 字符串）
     */
    @Override
    public void InsertRefreshToken(RefreshToken token) {
        String key = StringEncryptUtils.sha256HexString(REFRESH_TOKEN_PREFIX + token.getSignature());
        redisDaoUtils.set(key, token.getAccessTokenSignature());
        redisDaoUtils.expire(key, 30, TimeUnit.DAYS);
    }

    /**
     * 删除权限令牌
     *
     * @param signature
     * @return
     */
    @Override
    public void DeleteAccessToken(String signature) {
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(ACCESS_TOKEN_PREFIX + signature));
    }

    /**
     * 删除刷新令牌
     *
     * @param signature
     * @return
     */
    @Override
    public void DeleteRefreshToken(String signature) {
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(REFRESH_TOKEN_PREFIX + signature));
    }

    /**
     * 查询访问设备信息（Value 存 JSON 字符串）
     */
    @Override
    public Device QueryDeviceData(String signature) {
        String key = StringEncryptUtils.sha256HexString(DEVICE_DATA_PREFIX + signature);
        String json = redisDaoUtils.get(key);
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, Device.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis Device 反序列化失败", e);
        }
    }

    /**
     * 保存访问设备信息（存 JSON 字符串）
     */
    @Override
    public void SaveDeviceData(String signature, Device device) {
        String key = StringEncryptUtils.sha256HexString(DEVICE_DATA_PREFIX + signature);
        try {
            redisDaoUtils.set(key, objectMapper.writeValueAsString(device));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Device 序列化失败", e);
        }
        redisDaoUtils.expire(key, 7, TimeUnit.DAYS);
    }

    @Override
    public void InsertWebLoginToken(String token, String sessionId) {
        String tokenKey = WEB_LOGIN_TOKEN_PREFIX + token;
        String sessionKey = WEB_SESSION_TO_TOKEN_PREFIX + sessionId;
        redisDaoUtils.set(tokenKey, sessionId);
        redisDaoUtils.expire(tokenKey, WEB_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        redisDaoUtils.set(sessionKey, token);
        redisDaoUtils.expire(sessionKey, WEB_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    @Override
    public String QuerySessionIdByWebToken(String token) {
        return redisDaoUtils.get(WEB_LOGIN_TOKEN_PREFIX + token);
    }

    @Override
    public String QueryWebTokenBySessionId(String sessionId) {
        return redisDaoUtils.get(WEB_SESSION_TO_TOKEN_PREFIX + sessionId);
    }

    @Override
    public void DeleteWebLoginToken(String token) {
        String sessionId = redisDaoUtils.get(WEB_LOGIN_TOKEN_PREFIX + token);
        redisDaoUtils.delete(WEB_LOGIN_TOKEN_PREFIX + token);
        if (sessionId != null && !sessionId.isEmpty()) {
            redisDaoUtils.delete(WEB_SESSION_TO_TOKEN_PREFIX + sessionId);
        }
    }

    @Override
    public void DeleteWebLoginTokenBySessionId(String sessionId) {
        String token = redisDaoUtils.get(WEB_SESSION_TO_TOKEN_PREFIX + sessionId);
        redisDaoUtils.delete(WEB_SESSION_TO_TOKEN_PREFIX + sessionId);
        if (token != null && !token.isEmpty()) {
            redisDaoUtils.delete(WEB_LOGIN_TOKEN_PREFIX + token);
        }
    }

}
