package cn.gdeiassistant.common.redis.usercertificate;

import cn.gdeiassistant.common.pojo.encryption.AESEncryptConfig;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserCertificateDaoTest {

    @Test
    void loginCredentialIsWrittenWithTtlInOneCommandAndIndexedForUser() {
        AESEncryptConfig config = new AESEncryptConfig();
        config.setPrivateKey("synthetic-test-key");
        new StringEncryptUtils().setEncryptConfig(config);
        try {
            RedisTemplate<String, String> redis = mock(RedisTemplate.class);
            ValueOperations<String, String> values = mock(ValueOperations.class);
            when(redis.opsForValue()).thenReturn(values);
            UserCertificateDaoImpl dao = new UserCertificateDaoImpl(new ObjectMapper(), redis);

            String expectedLoginKey = UserCertificateDaoImpl.loginKey("session");
            String expectedIndexKey = UserCertificateDaoImpl.userIndexKey("owner");

            dao.saveUserLoginCertificate("session", "owner", "synthetic-password");

            verify(values).set(eq(expectedLoginKey), anyString(), eq(Duration.ofHours(1)));
            verify(redis, never()).expire(eq(expectedLoginKey), anyLong(), any(TimeUnit.class));
            verify(redis, never()).expire(eq(expectedLoginKey), any(Duration.class));
            verify(redis).execute(
                    eq(UserCertificateDaoImpl.INDEX_CREDENTIAL_SCRIPT),
                    eq(List.of(expectedIndexKey)),
                    eq(expectedLoginKey),
                    eq(String.valueOf(Duration.ofHours(1).getSeconds()))
            );
            verify(redis, never()).expire(eq(expectedIndexKey), any(Duration.class));
        } finally {
            new StringEncryptUtils().setEncryptConfig(null);
        }
    }

    @Test
    void indexUpdateScriptAtomicallyExtendsButNeverShrinksTtl() {
        AESEncryptConfig config = new AESEncryptConfig();
        config.setPrivateKey("synthetic-test-key");
        new StringEncryptUtils().setEncryptConfig(config);
        try {
            RedisTemplate<String, String> redis = mock(RedisTemplate.class);
            ValueOperations<String, String> values = mock(ValueOperations.class);
            when(redis.opsForValue()).thenReturn(values);
            String indexKey = UserCertificateDaoImpl.userIndexKey("owner");
            UserCertificateDaoImpl dao = new UserCertificateDaoImpl(new ObjectMapper(), redis);

            UserCertificateEntity session = new UserCertificateEntity();
            session.setUser(new User("owner", "synthetic-password"));
            session.setKeycode("k");
            session.setNumber("10000000001");
            session.setTimestamp(12L);

            dao.saveUserSessionCertificate("session", session);

            verify(redis).execute(
                    eq(UserCertificateDaoImpl.INDEX_CREDENTIAL_SCRIPT),
                    eq(List.of(indexKey)),
                    eq(UserCertificateDaoImpl.sessionKey("session")),
                    eq(String.valueOf(Duration.ofMinutes(10).getSeconds()))
            );
            verify(redis, never()).getExpire(eq(indexKey), any(TimeUnit.class));
            verify(redis, never()).expire(eq(indexKey), any(Duration.class));
            String lua = UserCertificateDaoImpl.INDEX_CREDENTIAL_SCRIPT.getScriptAsString();
            assertTrue(lua.contains("SADD"));
            assertTrue(lua.contains("if ttl < 0 or ttl < target then"));
        } finally {
            new StringEncryptUtils().setEncryptConfig(null);
        }
    }

    @Test
    void redisDeletionFailureCannotBeMistakenForSuccessfulRevocation() {
        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        SetOperations<String, String> sets = mock(SetOperations.class);
        when(redis.opsForSet()).thenReturn(sets);

        String indexKey = UserCertificateDaoImpl.userIndexKey("owner");
        String loginKey = UserCertificateDaoImpl.loginKey("session");
        String sessionKey = UserCertificateDaoImpl.sessionKey("session");
        when(sets.members(indexKey)).thenReturn(Set.of(loginKey, sessionKey));
        doThrow(new IllegalStateException("synthetic Redis unavailable"))
                .when(redis).delete(List.of(loginKey));

        UserCertificateDaoImpl dao = new UserCertificateDaoImpl(new ObjectMapper(), redis);

        assertThrows(
                RuntimeException.class, () -> dao.deleteUserLoginCertificatesByUsername("owner"));
        verify(redis, never()).execute(any(RedisCallback.class));
        verify(sets, never()).remove(eq(indexKey), any());
    }
}
