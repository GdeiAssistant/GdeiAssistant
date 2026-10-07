package cn.gdeiassistant.common.redis.usercertificate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.pojo.encryption.AESEncryptConfig;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

class UserCertificateDaoTest {
    @Test
    void loginCredentialIsWrittenWithTtlInOneCommand() {
        AESEncryptConfig config = new AESEncryptConfig();
        config.setPrivateKey("synthetic-test-key");
        new StringEncryptUtils().setEncryptConfig(config);
        try {
            RedisTemplate<String, String> redis = mock(RedisTemplate.class);
            ValueOperations<String, String> values = mock(ValueOperations.class);
            when(redis.opsForValue()).thenReturn(values);
            UserCertificateDaoImpl dao = new UserCertificateDaoImpl();
            ReflectionTestUtils.setField(dao, "redisTemplate", redis);
            ReflectionTestUtils.setField(dao, "objectMapper", new ObjectMapper());
            dao.saveUserLoginCertificate("session", "owner", "synthetic-password");
            verify(values).set(anyString(), anyString(), eq(1L), eq(TimeUnit.HOURS));
            verify(redis, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
        } finally {
            new StringEncryptUtils().setEncryptConfig(null);
        }
    }

    @Test
    void redisDeletionFailureCannotBeMistakenForSuccessfulRevocation() {
        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        String key = "a".repeat(64);
        when(values.get(key))
                .thenReturn("{\"username\":\"owner\",\"password\":\"synthetic-encrypted\"}");
        org.springframework.data.redis.connection.RedisConnection connection =
                mock(org.springframework.data.redis.connection.RedisConnection.class);
        org.springframework.data.redis.connection.RedisKeyCommands keys =
                mock(org.springframework.data.redis.connection.RedisKeyCommands.class);
        Cursor<byte[]> cursor = mock(Cursor.class);
        when(connection.keyCommands()).thenReturn(keys);
        when(keys.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(redis.execute(any(RedisCallback.class)))
                .thenAnswer(i -> ((RedisCallback<?>) i.getArgument(0)).doInRedis(connection));
        doThrow(new IllegalStateException("synthetic Redis unavailable")).when(redis).delete(key);
        UserCertificateDaoImpl dao = new UserCertificateDaoImpl();
        ReflectionTestUtils.setField(dao, "redisTemplate", redis);
        ReflectionTestUtils.setField(dao, "objectMapper", new ObjectMapper());
        assertThrows(
                RuntimeException.class, () -> dao.deleteUserLoginCertificatesByUsername("owner"));
    }
}
