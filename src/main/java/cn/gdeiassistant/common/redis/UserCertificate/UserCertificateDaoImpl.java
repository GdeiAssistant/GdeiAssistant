package cn.gdeiassistant.common.redis.UserCertificate;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.common.tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class UserCertificateDaoImpl implements UserCertificateDao {

    private static final TypeReference<Map<String, String>> MAP_STRING_STRING = new TypeReference<Map<String, String>>() {};

    private final String COOKIE_PREFIX = "USER_COOKIE_CERTIFICATE_";

    private final String LOGIN_PREFIX = "USER_LOGIN_CERTIFICATE_";

    private final String SESSION_PREFIX = "USER_SESSION_CERTIFICATE_";

    private String encryptPassword(String password) {
        try {
            return StringEncryptUtils.encryptString(password);
        } catch (Exception e) {
            throw new RuntimeException("Password encryption failed", e);
        }
    }

    private String decryptPassword(String encrypted) {
        try {
            return StringEncryptUtils.decryptString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Password decryption failed", e);
        }
    }

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public User queryUserCookieCertificate(String cookieId) {
        String key = StringEncryptUtils.sha256HexString(COOKIE_PREFIX + cookieId);
        String json = redisDaoUtils.get(key);
        if (json == null || json.isEmpty()) return null;
        try {
            Map<String, String> map = objectMapper.readValue(json, MAP_STRING_STRING);
            User user = new User();
            user.setUsername(map.get("username"));
            user.setPassword(decryptPassword(map.get("password")));
            return user;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 登录凭证反序列化失败", e);
        }
    }

    @Override
    public void saveUserCookieCertificate(String cookieId, String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", encryptPassword(password));
        String key = StringEncryptUtils.sha256HexString(COOKIE_PREFIX + cookieId);
        try {
            redisDaoUtils.set(key, objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("登录凭证序列化失败", e);
        }
        redisDaoUtils.expire(key, 7, TimeUnit.DAYS);
    }

    @Override
    public void updateUserCookieCertificateExpiration(String cookieId) {
        redisDaoUtils.expire(StringEncryptUtils.sha256HexString(COOKIE_PREFIX + cookieId), 7, TimeUnit.DAYS);
    }

    @Override
    public User queryUserLoginCertificate(String sessionId) {
        String finalKey = StringEncryptUtils.sha256HexString(LOGIN_PREFIX + sessionId);
        if (redisTemplate != null) {
            try {
                String json = redisTemplate.opsForValue().get(finalKey);
                if (json != null && !json.isEmpty()) {
                    Map<String, String> map = objectMapper.readValue(json, MAP_STRING_STRING);
                    User user = new User();
                    user.setUsername(map.get("username"));
                    user.setPassword(decryptPassword(map.get("password")));
                    return user;
                }
            } catch (Exception e) {
                throw new RuntimeException("Redis 读取登录凭证失败", e);
            }
        }
        return null;
    }

    @Override
    public void updateUserLoginCertificateExpiration(String sessionId) {
        redisDaoUtils.expire(StringEncryptUtils.sha256HexString(LOGIN_PREFIX + sessionId), 1, TimeUnit.HOURS);
    }

    @Override
    public void deleteUserLoginCertificate(String sessionId) {
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(LOGIN_PREFIX + sessionId));
    }

    @Override
    public void deleteUserSessionCertificate(String sessionId) {
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(SESSION_PREFIX + sessionId));
    }

    @Override
    public void saveUserLoginCertificate(String sessionId, String username, String password) {
        if (redisTemplate == null) return;
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", encryptPassword(password));
        String finalKey = StringEncryptUtils.sha256HexString(LOGIN_PREFIX + sessionId);
        try {
            redisTemplate.opsForValue().set(finalKey, objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("登录凭证序列化失败", e);
        }
        redisTemplate.expire(finalKey, 1, TimeUnit.HOURS);
    }

    @Override
    public UserCertificateEntity queryUserSessionCertificate(String sessionId) {
        String key = StringEncryptUtils.sha256HexString(SESSION_PREFIX + sessionId);
        String json = redisDaoUtils.get(key);
        if (json == null || json.isEmpty()) return null;
        try {
            Map<String, String> map = objectMapper.readValue(json, MAP_STRING_STRING);
            UserCertificateEntity entity = new UserCertificateEntity();
            User user = new User();
            user.setUsername(map.get("username"));
            user.setPassword(decryptPassword(map.get("password")));
            entity.setUser(user);
            entity.setKeycode(map.get("keycode"));
            entity.setNumber(map.get("number"));
            entity.setTimestamp(Long.valueOf(map.get("timestamp")));
            return entity;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 会话凭证反序列化失败", e);
        }
    }

    @Override
    public void saveUserSessionCertificate(String sessionId, UserCertificateEntity userCertificate) {
        Map<String, String> map = new HashMap<>();
        map.put("username", userCertificate.getUser().getUsername());
        map.put("password", encryptPassword(userCertificate.getUser().getPassword()));
        map.put("keycode", userCertificate.getKeycode());
        map.put("number", userCertificate.getNumber());
        map.put("timestamp", String.valueOf(userCertificate.getTimestamp()));
        String key = StringEncryptUtils.sha256HexString(SESSION_PREFIX + sessionId);
        try {
            redisDaoUtils.set(key, objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("会话凭证序列化失败", e);
        }
        redisDaoUtils.expire(key, 10, TimeUnit.MINUTES);
    }
}
