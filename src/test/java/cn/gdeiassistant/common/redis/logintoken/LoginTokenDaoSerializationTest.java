package cn.gdeiassistant.common.redis.logintoken;

import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.common.tools.springutils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginTokenDaoSerializationTest {
    private final RedisDaoUtils redis = mock(RedisDaoUtils.class);
    private final LoginTokenDaoImpl dao = new LoginTokenDaoImpl();
    private final Map<String, String> values = new HashMap<>();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(dao, "redisDaoUtils", redis);
        ReflectionTestUtils.setField(dao, "objectMapper", new ObjectMapper());
        when(redis.get(anyString())).thenAnswer(a -> values.get(a.getArgument(0)));
        doAnswer(a -> { values.put(a.getArgument(0), a.getArgument(1)); return null; })
                .when(redis).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }
    @Test void tokenAndDeviceRoundTripUseSeparateHashedKeysAndFiniteTtl() {
        AccessToken access = new AccessToken(); access.setSignature("access"); access.setUsername("synthetic-owner");
        dao.insertAccessToken(access);
        assertEquals("synthetic-owner", dao.queryAccessToken("access").getUsername());
        verify(redis).set(eq(key("ACCESS_TOKEN_access")), anyString(), eq(7L), eq(TimeUnit.DAYS));
        RefreshToken refresh = new RefreshToken(); refresh.setSignature("refresh"); refresh.setAccessTokenSignature("access");
        dao.insertRefreshToken(refresh);
        assertEquals("access", dao.queryRefreshToken("refresh").getAccessTokenSignature());
        assertEquals("refresh", dao.queryRefreshToken("refresh").getSignature());
        verify(redis).set(key("REFRESH_TOKEN_refresh"), "access", 30, TimeUnit.DAYS);
        Device device = new Device(); device.setUnionID("synthetic-device"); device.setIP("127.0.0.1");
        dao.saveDeviceData("access", device);
        assertEquals("synthetic-device", dao.queryDeviceData("access").getUnionID());
        assertEquals("127.0.0.1", dao.queryDeviceData("access").getIP());
        verify(redis).set(eq(key("DEVICE_DATA_access")), anyString(), eq(7L), eq(TimeUnit.DAYS));
        dao.deleteAccessToken("access"); dao.deleteRefreshToken("refresh");
        verify(redis).delete(key("ACCESS_TOKEN_access")); verify(redis).delete(key("REFRESH_TOKEN_refresh"));
    }
    @Test void absentOrEmptyRecordsAreNotAuthenticated() {
        assertNull(dao.queryAccessToken("missing")); assertNull(dao.queryRefreshToken("missing")); assertNull(dao.queryDeviceData("missing"));
        values.put(key("ACCESS_TOKEN_empty"), ""); values.put(key("REFRESH_TOKEN_empty"), ""); values.put(key("DEVICE_DATA_empty"), "");
        assertNull(dao.queryAccessToken("empty")); assertNull(dao.queryRefreshToken("empty")); assertNull(dao.queryDeviceData("empty"));
    }
    @Test void corruptedRecordsFailRatherThanYieldAnIdentity() {
        values.put(key("ACCESS_TOKEN_bad"), "not-json"); values.put(key("DEVICE_DATA_bad"), "not-json");
        assertThrows(RuntimeException.class, () -> dao.queryAccessToken("bad"));
        assertThrows(RuntimeException.class, () -> dao.queryDeviceData("bad"));
    }
    private static String key(String value) { return StringEncryptUtils.sha256HexString(value); }
}
