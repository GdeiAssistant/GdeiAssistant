package cn.gdeiassistant.common.redis.usercertificate;

import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class UserCertificateDaoImpl implements UserCertificateDao {

    private static final TypeReference<Map<String, String>> MAP_STRING_STRING = new TypeReference<Map<String, String>>() {};
    static final DefaultRedisScript<Long> INDEX_CREDENTIAL_SCRIPT = new DefaultRedisScript<>(
            "redis.call('SADD', KEYS[1], ARGV[1]); " +
                    "local ttl = redis.call('TTL', KEYS[1]); " +
                    "local target = tonumber(ARGV[2]); " +
                    "if ttl < 0 or ttl < target then redis.call('EXPIRE', KEYS[1], target) end; " +
                    "return 1;",
            Long.class
    );

    static final String COOKIE_KEY_PREFIX = "cert:cookie:";
    static final String LOGIN_KEY_PREFIX = "cert:login:";
    static final String SESSION_KEY_PREFIX = "cert:session:";
    static final String USER_INDEX_PREFIX = "cert:user:index:";

    private static final Duration COOKIE_TTL = Duration.ofDays(7);
    private static final Duration LOGIN_TTL = Duration.ofHours(1);
    private static final Duration SESSION_TTL = Duration.ofMinutes(10);

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public UserCertificateDaoImpl(ObjectMapper objectMapper,
                                  @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    static String cookieKey(String cookieId) {
        return COOKIE_KEY_PREFIX + StringEncryptUtils.sha256HexString(cookieId);
    }

    static String loginKey(String sessionId) {
        return LOGIN_KEY_PREFIX + StringEncryptUtils.sha256HexString(sessionId);
    }

    static String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + StringEncryptUtils.sha256HexString(sessionId);
    }

    static String userIndexKey(String username) {
        return USER_INDEX_PREFIX + StringEncryptUtils.sha256HexString(username);
    }

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

    @Override
    public User queryUserCookieCertificate(String cookieId) {
        return queryUserCredential(cookieKey(cookieId), "common.redis.loginCredentialDeserializeFailed");
    }

    @Override
    public void saveUserCookieCertificate(String cookieId, String username, String password) {
        saveUserCredential(cookieKey(cookieId), username, password, COOKIE_TTL);
    }

    @Override
    public void updateUserCookieCertificateExpiration(String cookieId) {
        refreshCredentialExpiration(cookieKey(cookieId), COOKIE_TTL);
    }

    @Override
    public User queryUserLoginCertificate(String sessionId) {
        return queryUserCredential(loginKey(sessionId), "common.redis.loginCredentialReadFailed");
    }

    @Override
    public void updateUserLoginCertificateExpiration(String sessionId) {
        refreshCredentialExpiration(loginKey(sessionId), LOGIN_TTL);
    }

    @Override
    public void deleteUserLoginCertificate(String sessionId) {
        deleteCredentialByKey(loginKey(sessionId));
    }

    @Override
    public void deleteUserSessionCertificate(String sessionId) {
        deleteCredentialByKey(sessionKey(sessionId));
    }

    @Override
    public void deleteUserLoginCertificatesByUsername(String username) {
        deleteCredentialsByUsername(username, false);
    }

    @Override
    public void deleteUserSessionCertificatesByUsername(String username) {
        deleteCredentialsByUsername(username, true);
    }

    @Override
    public void saveUserLoginCertificate(String sessionId, String username, String password) {
        saveUserCredential(loginKey(sessionId), username, password, LOGIN_TTL);
    }

    @Override
    public UserCertificateEntity queryUserSessionCertificate(String sessionId) {
        if (redisTemplate == null) {
            return null;
        }
        String json = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (json == null || json.isEmpty()) {
            return null;
        }
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
        if (redisTemplate == null) {
            return;
        }
        String username = userCertificate.getUser().getUsername();
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", encryptPassword(userCertificate.getUser().getPassword()));
        map.put("keycode", userCertificate.getKeycode());
        map.put("number", userCertificate.getNumber());
        map.put("timestamp", String.valueOf(userCertificate.getTimestamp()));
        String key = sessionKey(sessionId);
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(map), SESSION_TTL);
            indexCredentialKey(username, key, SESSION_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(Messages.get("common.redis.sessionCredentialSerializeFailed"), e);
        }
    }

    private User queryUserCredential(String key, String failureMessageKey) {
        if (redisTemplate == null) {
            return null;
        }
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            Map<String, String> map = objectMapper.readValue(json, MAP_STRING_STRING);
            User user = new User();
            user.setUsername(map.get("username"));
            user.setPassword(decryptPassword(map.get("password")));
            return user;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(Messages.get(failureMessageKey), e);
        }
    }

    private void saveUserCredential(String key, String username, String password, Duration ttl) {
        if (redisTemplate == null) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", encryptPassword(password));
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(map), ttl);
            indexCredentialKey(username, key, ttl);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(Messages.get("common.redis.loginCredentialSerializeFailed"), e);
        }
    }

    private void refreshCredentialExpiration(String key, Duration ttl) {
        if (redisTemplate == null) {
            return;
        }
        String json = redisTemplate.opsForValue().get(key);
        redisTemplate.expire(key, ttl);
        if (json != null && !json.isEmpty()) {
            try {
                Map<String, String> map = objectMapper.readValue(json, MAP_STRING_STRING);
                String username = map.get("username");
                if (username != null && !username.isBlank()) {
                    indexCredentialKey(username, key, ttl);
                }
            } catch (JsonProcessingException ignored) {
                // Keep TTL refresh resilient if an existing payload is malformed.
            }
        }
    }

    private void deleteCredentialByKey(String key) {
        if (redisTemplate == null) {
            return;
        }
        String json = redisTemplate.opsForValue().get(key);
        if (json != null && !json.isEmpty()) {
            try {
                Map<String, String> map = objectMapper.readValue(json, MAP_STRING_STRING);
                String username = map.get("username");
                if (username != null && !username.isBlank()) {
                    redisTemplate.opsForSet().remove(userIndexKey(username), key);
                }
            } catch (JsonProcessingException ignored) {
                // Still delete corrupt credential keys.
            }
        }
        redisTemplate.delete(key);
    }

    private void indexCredentialKey(String username, String key, Duration ttl) {
        if (username == null || username.isBlank()) {
            return;
        }
        redisTemplate.execute(
                INDEX_CREDENTIAL_SCRIPT,
                List.of(userIndexKey(username)),
                key,
                String.valueOf(ttl.getSeconds())
        );
    }

    private void deleteCredentialsByUsername(String username, boolean sessionOnly) {
        if (redisTemplate == null || username == null || username.isBlank()) {
            return;
        }
        String indexKey = userIndexKey(username);
        Set<String> indexedKeys = redisTemplate.opsForSet().members(indexKey);
        if (indexedKeys == null || indexedKeys.isEmpty()) {
            return;
        }
        List<String> keysToDelete = new ArrayList<>();
        for (String key : indexedKeys) {
            if (key == null) {
                continue;
            }
            boolean isSession = key.startsWith(SESSION_KEY_PREFIX);
            boolean isLoginOrCookie = key.startsWith(LOGIN_KEY_PREFIX) || key.startsWith(COOKIE_KEY_PREFIX);
            if ((sessionOnly && isSession) || (!sessionOnly && isLoginOrCookie)) {
                keysToDelete.add(key);
            }
        }
        if (keysToDelete.isEmpty()) {
            return;
        }
        redisTemplate.delete(keysToDelete);
        redisTemplate.opsForSet().remove(indexKey, keysToDelete.toArray());
    }
}
