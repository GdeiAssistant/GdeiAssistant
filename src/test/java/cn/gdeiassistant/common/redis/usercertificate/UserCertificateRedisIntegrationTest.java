package cn.gdeiassistant.common.redis.usercertificate;

import static org.junit.jupiter.api.Assertions.*;

import cn.gdeiassistant.common.config.redis.RedisConfig;
import cn.gdeiassistant.common.pojo.encryption.AESEncryptConfig;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.springutils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import cn.gdeiassistant.core.userlogin.pojo.entity.UserCertificateEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

/** Dedicated synthetic Redis only. Never consumes application .env or cloud settings. */
@EnabledIfEnvironmentVariable(named = "GDEI_REDIS_TEST_PORT", matches = "\\d+")
class UserCertificateRedisIntegrationTest {
    LettuceConnectionFactory factory;
    RedisTemplate<String, String> redis;
    UserCertificateDaoImpl dao;
    RedisDaoUtils utils;
    final String namespace = java.util.UUID.randomUUID().toString();
    String owner = namespace + "-owner", other = namespace + "-other";

    @BeforeEach
    void setup() {
        factory =
                new LettuceConnectionFactory(
                        "127.0.0.1", Integer.parseInt(System.getenv("GDEI_REDIS_TEST_PORT")));
        factory.afterPropertiesSet();
        factory.start();
        redis = new RedisConfig().redisTemplate(factory);
        utils = new RedisDaoUtils();
        ReflectionTestUtils.setField(utils, "redisTemplate", redis);
        dao = new UserCertificateDaoImpl();
        ReflectionTestUtils.setField(dao, "redisDaoUtils", utils);
        ReflectionTestUtils.setField(dao, "redisTemplate", redis);
        ReflectionTestUtils.setField(dao, "objectMapper", new ObjectMapper());
        AESEncryptConfig c = new AESEncryptConfig();
        c.setPrivateKey("synthetic-integration-encryption");
        new StringEncryptUtils().setEncryptConfig(c);
    }

    @AfterEach
    void close() {
        try {
            dao.deleteUserSessionCertificatesByUsername(owner);
            dao.deleteUserLoginCertificatesByUsername(owner);
            dao.deleteUserLoginCertificatesByUsername(other);
        } finally {
            factory.destroy();
            new StringEncryptUtils().setEncryptConfig(null);
        }
    }

    String key(String prefix, String id) {
        return StringEncryptUtils.sha256HexString(prefix + id);
    }

    UserCertificateEntity session() {
        UserCertificateEntity e = new UserCertificateEntity();
        User u = new User(owner);
        u.setPassword("synthetic-password");
        e.setUser(u);
        e.setKeycode("synthetic");
        e.setNumber("10000000001");
        e.setTimestamp(12L);
        return e;
    }

    void ttl(String key, long seconds) {
        Long t = redis.getExpire(key, TimeUnit.SECONDS);
        assertNotNull(t);
        assertTrue(t > seconds - 20 && t <= seconds, "credential must have bounded TTL");
    }

    @Test
    void allCredentialTypesRoundTripEncryptedWithBoundedTtl() {
        dao.saveUserCookieCertificate(namespace, owner, "synthetic-password");
        dao.saveUserLoginCertificate(namespace, owner, "synthetic-password");
        dao.saveUserSessionCertificate(namespace, session());
        assertEquals("synthetic-password", dao.queryUserCookieCertificate(namespace).getPassword());
        assertEquals(owner, dao.queryUserLoginCertificate(namespace).getUsername());
        UserCertificateEntity e = dao.queryUserSessionCertificate(namespace);
        assertEquals("synthetic-password", e.getUser().getPassword());
        assertEquals("10000000001", e.getNumber());
        assertEquals("synthetic", e.getKeycode());
        assertEquals(12L, e.getTimestamp());
        for (String prefix :
                new String[] {
                    "USER_COOKIE_CERTIFICATE_",
                    "USER_LOGIN_CERTIFICATE_",
                    "USER_SESSION_CERTIFICATE_"
                }) {
            assertFalse(
                    redis.opsForValue().get(key(prefix, namespace)).contains("synthetic-password"));
        }
        ttl(key("USER_COOKIE_CERTIFICATE_", namespace), 7 * 86400);
        ttl(key("USER_LOGIN_CERTIFICATE_", namespace), 3600);
        ttl(key("USER_SESSION_CERTIFICATE_", namespace), 600);
    }

    @Test
    void refreshingAndDeletingUsesSeparateCredentialNamespaces() {
        dao.saveUserCookieCertificate(namespace, owner, "synthetic-password");
        dao.saveUserLoginCertificate(namespace, owner, "synthetic-password");
        dao.saveUserSessionCertificate(namespace, session());
        redis.expire(key("USER_COOKIE_CERTIFICATE_", namespace), 1, TimeUnit.SECONDS);
        redis.expire(key("USER_LOGIN_CERTIFICATE_", namespace), 1, TimeUnit.SECONDS);
        dao.updateUserCookieCertificateExpiration(namespace);
        dao.updateUserLoginCertificateExpiration(namespace);
        ttl(key("USER_COOKIE_CERTIFICATE_", namespace), 7 * 86400);
        ttl(key("USER_LOGIN_CERTIFICATE_", namespace), 3600);
        dao.deleteUserLoginCertificate(namespace);
        dao.deleteUserSessionCertificate(namespace);
        assertNull(dao.queryUserLoginCertificate(namespace));
        assertNull(dao.queryUserSessionCertificate(namespace));
        assertNotNull(dao.queryUserCookieCertificate(namespace));
    }

    @Test
    void userWideRevocationPreservesOtherUsersAndUnrelatedCache() {
        dao.saveUserCookieCertificate(namespace, owner, "synthetic-password");
        dao.saveUserLoginCertificate(namespace, owner, "synthetic-password");
        dao.saveUserSessionCertificate(namespace, session());
        dao.saveUserLoginCertificate(namespace + "other", other, "synthetic-password");
        String unrelated = key("unrelated", namespace),
                malformed = key("malformed", namespace),
                plain = namespace + "-plain";
        redis.opsForValue().set(unrelated, "{\"username\":\"" + owner + "\",\"value\":\"keep\"}");
        redis.opsForValue().set(malformed, "invalid-json");
        redis.opsForValue().set(plain, "keep");
        try {
            dao.deleteUserSessionCertificatesByUsername(owner);
            assertNull(dao.queryUserSessionCertificate(namespace));
            assertNotNull(dao.queryUserLoginCertificate(namespace));
            dao.deleteUserLoginCertificatesByUsername(owner);
            assertNull(dao.queryUserCookieCertificate(namespace));
            assertNull(dao.queryUserLoginCertificate(namespace));
            assertNotNull(dao.queryUserLoginCertificate(namespace + "other"));
            assertTrue(redis.hasKey(unrelated));
            assertTrue(redis.hasKey(malformed));
            assertTrue(redis.hasKey(plain));
        } finally {
            redis.delete(java.util.List.of(unrelated, malformed, plain));
        }
    }

    @Test
    void absentAndCorruptCredentialsNeverBecomeAuthenticatedUsers() {
        assertNull(dao.queryUserCookieCertificate(namespace));
        assertNull(dao.queryUserLoginCertificate(namespace));
        assertNull(dao.queryUserSessionCertificate(namespace));
        String k = key("USER_LOGIN_CERTIFICATE_", namespace);
        redis.opsForValue().set(k, "invalid-json");
        try {
            assertThrows(RuntimeException.class, () -> dao.queryUserLoginCertificate(namespace));
        } finally {
            redis.delete(k);
        }
    }

    @Test
    void atomicReservationAndSerializedCookieStoreRoundTrip() {
        String k = namespace + "-reservation", serialized = namespace + "-serialized";
        try {
            assertTrue(utils.setIfAbsent(k, "first", 20, TimeUnit.SECONDS));
            assertFalse(utils.setIfAbsent(k, "second", 20, TimeUnit.SECONDS));
            assertEquals("first", utils.get(k));
            ttl(k, 20);
            utils.setSerializable(
                    serialized, new java.util.ArrayList<>(java.util.List.of("synthetic-cookie")));
            assertEquals(java.util.List.of("synthetic-cookie"), utils.getSerializable(serialized));
            redis.opsForValue().set(serialized, "corrupt");
            assertThrows(RuntimeException.class, () -> utils.getSerializable(serialized));
        } finally {
            utils.delete(k);
            utils.delete(serialized);
        }
    }
}
