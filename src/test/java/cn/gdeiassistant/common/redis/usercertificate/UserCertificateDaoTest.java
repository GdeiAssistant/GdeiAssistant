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
            SetOperations<String, String> sets = mock(SetOperations.class);
            when(redis.opsForValue()).thenReturn(values);
            when(redis.opsForSet()).thenReturn(sets);
            UserCertificateDaoImpl dao = new UserCertificateDaoImpl(new ObjectMapper(), redis);

            String expectedLoginKey = UserCertificateDaoImpl.loginKey("session");
            String expectedIndexKey = UserCertificateDaoImpl.userIndexKey("owner");

            dao.saveUserLoginCertificate("session", "owner", "synthetic-password");

            verify(values).set(eq(expectedLoginKey), anyString(), eq(Duration.ofHours(1)));
            verify(redis, never()).expire(eq(expectedLoginKey), anyLong(), any(TimeUnit.class));
            verify(redis, never()).expire(eq(expectedLoginKey), any(Duration.class));
            verify(sets).add(expectedIndexKey, expectedLoginKey);
            verify(redis).expire(expectedIndexKey, Duration.ofHours(1));
        } finally {
            new StringEncryptUtils().setEncryptConfig(null);
        }
    }

    @Test
    void indexTtlDoesNotShrinkWhenShorterLivedSessionIsSavedAfterCookie() {
        AESEncryptConfig config = new AESEncryptConfig();
        config.setPrivateKey("synthetic-test-key");
        new StringEncryptUtils().setEncryptConfig(config);
        try {
            RedisTemplate<String, String> redis = mock(RedisTemplate.class);
            ValueOperations<String, String> values = mock(ValueOperations.class);
            SetOperations<String, String> sets = mock(SetOperations.class);
            when(redis.opsForValue()).thenReturn(values);
            when(redis.opsForSet()).thenReturn(sets);
            String indexKey = UserCertificateDaoImpl.userIndexKey("owner");
            when(redis.getExpire(indexKey, TimeUnit.SECONDS)).thenReturn(604000L);
            UserCertificateDaoImpl dao = new UserCertificateDaoImpl(new ObjectMapper(), redis);

            UserCertificateEntity session = new UserCertificateEntity();
            session.setUser(new User("owner", "synthetic-password"));
            session.setKeycode("k");
            session.setNumber("10000000001");
            session.setTimestamp(12L);

            dao.saveUserSessionCertificate("session", session);

            verify(sets).add(indexKey, UserCertificateDaoImpl.sessionKey("session"));
            verify(redis, never()).expire(eq(indexKey), any(Duration.class));
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
