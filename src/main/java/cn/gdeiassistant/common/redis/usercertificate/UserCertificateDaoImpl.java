package cn.gdeiassistant.common.redis.usercertificate;

import org.springframework.beans.factory.annotation.Qualifier;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.common.tools.springutils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import cn.gdeiassistant.common.i18n.Messages;

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

    private final RedisDaoUtils redisDaoUtils;

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, String> redisTemplate;

    public UserCertificateDaoImpl(RedisDaoUtils redisDaoUtils, ObjectMapper objectMapper, @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisDaoUtils = redisDaoUtils;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

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
            throw new RuntimeException(Messages.get("common.redis.loginCredentialDeserializeFailed"), e);
        }
    }

    @Override
    public void saveUserCookieCertificate(String cookieId, String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", encryptPassword(password));
        String key = StringEncryptUtils.sha256HexString(COOKIE_PREFIX + cookieId);
        try {
            redisDaoUtils.set(key, objectMapper.writeValueAsString(map), 7, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(Messages.get("common.redis.loginCredentialSerializeFailed"), e);
        }
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
                throw new RuntimeException(Messages.get("common.redis.loginCredentialReadFailed"), e);
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
    public void deleteUserLoginCertificatesByUsername(String username) {
        deleteCredentialEntriesByUsername(username, false);
    }

    @Override
    public void deleteUserSessionCertificatesByUsername(String username) {
        deleteCredentialEntriesByUsername(username, true);
    }

    @Override
    public void saveUserLoginCertificate(String sessionId, String username, String password) {
        if (redisTemplate == null) return;
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", encryptPassword(password));
        String finalKey = StringEncryptUtils.sha256HexString(LOGIN_PREFIX + sessionId);
        try {
            redisTemplate.opsForValue().set(finalKey, objectMapper.writeValueAsString(map), java.time.Duration.ofHours(1));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(Messages.get("common.redis.loginCredentialSerializeFailed"), e);
        }
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
            throw new RuntimeException(Messages.get("common.redis.sessionCredentialDeserializeFailed"), e);
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
            redisDaoUtils.set(key, objectMapper.writeValueAsString(map), 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(Messages.get("common.redis.sessionCredentialSerializeFailed"), e);
        }
    }

    private void deleteCredentialEntriesByUsername(String username, boolean requireSessionShape) {
        if (redisTemplate == null || username == null || username.isBlank()) {
            return;
        }
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions scanOptions = ScanOptions.scanOptions().count(1000).build();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(scanOptions)) {
                while (cursor.hasNext()) {
                    String key = new String(cursor.next(), StandardCharsets.UTF_8);
                    if (!isHashedCredentialKey(key)) {
                        continue;
                    }
                    deleteCredentialEntryIfMatches(username, requireSessionShape, key);
                }
            } catch (Exception e) {
                throw new RuntimeException(Messages.get("common.redis.credentialScanFailed"), e);
            }
            return null;
        });
    }

    private void deleteCredentialEntryIfMatches(String username, boolean requireSessionShape, String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return;
        }
        Map<String, String> map;
        try {
            map = objectMapper.readValue(json, MAP_STRING_STRING);
        } catch (JsonProcessingException ignored) {
            // Ignore unrelated or malformed values, but never swallow a Redis deletion failure.
            return;
        }
        if (!username.equals(map.get("username")) || !map.containsKey("password")) {
            return;
        }
        boolean looksLikeSessionCredential = map.containsKey("keycode")
                && map.containsKey("number") && map.containsKey("timestamp");
        if (requireSessionShape == looksLikeSessionCredential) {
            redisTemplate.delete(key);
        }
    }

    private boolean isHashedCredentialKey(String key) {
        if (key == null || key.length() != 64) {
            return false;
        }
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }
        return true;
    }
}
