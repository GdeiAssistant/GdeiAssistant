package cn.gdeiassistant.core.charge.service;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.ChargeException.ChargeIdempotencyException;
import cn.gdeiassistant.common.tools.SpringUtils.RedisDaoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargeIdempotencyServiceTest {

    private static final String USERNAME = "synthetic_charge_user";
    private static final String OTHER_USERNAME = "synthetic_other_charge_user";
    private static final String DEVICE_ID = "synthetic-device-for-charge-test";
    private static final String IDEMPOTENCY_KEY = "synthetic-idempotency-key";

    @Mock
    private RedisDaoUtils redisDaoUtils;

    private ObjectMapper objectMapper;

    private ChargeIdempotencyService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        service = new ChargeIdempotencyService();
        ReflectionTestUtils.setField(service, "redisDaoUtils", redisDaoUtils);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
    }

    @Test
    void shouldCreateProcessingRecordWithoutRawSensitiveValues() throws Exception {
        when(redisDaoUtils.setIfAbsent(anyString(), anyString(), eq(15L), eq(TimeUnit.MINUTES)))
                .thenReturn(true);

        ChargeIdempotencyService.ChargeIdempotencyContext context = service.begin(USERNAME,
                ChargeIdempotencyService.ENDPOINT_CARD_CHARGE, IDEMPOTENCY_KEY, 50, DEVICE_ID);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisDaoUtils).setIfAbsent(keyCaptor.capture(), valueCaptor.capture(),
                eq(15L), eq(TimeUnit.MINUTES));

        assertNotNull(context);
        assertTrue(keyCaptor.getValue().startsWith("charge:idempotency:"));
        assertFalse(keyCaptor.getValue().contains(IDEMPOTENCY_KEY));
        assertFalse(keyCaptor.getValue().contains(USERNAME));
        assertFalse(valueCaptor.getValue().contains(IDEMPOTENCY_KEY));
        assertFalse(valueCaptor.getValue().contains(DEVICE_ID));
        assertFalse(valueCaptor.getValue().contains("synthetic-charge-password"));
        assertFalse(valueCaptor.getValue().contains("token"));
        assertFalse(valueCaptor.getValue().contains("session"));
        assertFalse(valueCaptor.getValue().contains("cookie"));
        assertEquals("PROCESSING", objectMapper.readValue(valueCaptor.getValue(),
                ChargeIdempotencyService.ChargeIdempotencyRecord.class).getStatus());
    }

    @Test
    void shouldRejectDuplicateProcessingRequest() throws Exception {
        ChargeIdempotencyService.ChargeIdempotencyContext context = createInitialContext(50);
        String existingRecord = recordJson("PROCESSING", context.getFingerprint());
        when(redisDaoUtils.setIfAbsent(anyString(), anyString(), eq(15L), eq(TimeUnit.MINUTES)))
                .thenReturn(false);
        when(redisDaoUtils.get(anyString())).thenReturn(existingRecord);

        ChargeIdempotencyException exception = assertThrows(ChargeIdempotencyException.class,
                () -> service.begin(USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE,
                        IDEMPOTENCY_KEY, 50, DEVICE_ID));

        assertEquals(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT, exception.getCode());
    }

    @Test
    void shouldRejectDuplicateSuccessRequest() throws Exception {
        ChargeIdempotencyService.ChargeIdempotencyContext context = createInitialContext(50);
        String existingRecord = recordJson("SUCCESS", context.getFingerprint());
        when(redisDaoUtils.setIfAbsent(anyString(), anyString(), eq(15L), eq(TimeUnit.MINUTES)))
                .thenReturn(false);
        when(redisDaoUtils.get(anyString())).thenReturn(existingRecord);

        ChargeIdempotencyException exception = assertThrows(ChargeIdempotencyException.class,
                () -> service.begin(USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE,
                        IDEMPOTENCY_KEY, 50, DEVICE_ID));

        assertEquals(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT, exception.getCode());
        assertTrue(exception.getMessage().contains("已处理"));
    }

    @Test
    void shouldRejectSameKeyWithDifferentPayloadFingerprint() throws Exception {
        ChargeIdempotencyService.ChargeIdempotencyContext context = createInitialContext(50);
        String existingRecord = recordJson("PROCESSING", context.getFingerprint());
        when(redisDaoUtils.setIfAbsent(anyString(), anyString(), eq(15L), eq(TimeUnit.MINUTES)))
                .thenReturn(false);
        when(redisDaoUtils.get(anyString())).thenReturn(existingRecord);

        ChargeIdempotencyException exception = assertThrows(ChargeIdempotencyException.class,
                () -> service.begin(USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE,
                        IDEMPOTENCY_KEY, 60, DEVICE_ID));

        assertEquals(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT, exception.getCode());
        assertEquals("同一幂等键不能用于不同充值参数", exception.getMessage());
    }

    @Test
    void shouldScopeSameKeyByUser() throws Exception {
        when(redisDaoUtils.setIfAbsent(anyString(), anyString(), eq(15L), eq(TimeUnit.MINUTES)))
                .thenReturn(true);

        service.begin(USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE, IDEMPOTENCY_KEY, 50, DEVICE_ID);
        service.begin(OTHER_USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE, IDEMPOTENCY_KEY, 50, DEVICE_ID);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisDaoUtils, org.mockito.Mockito.times(2)).setIfAbsent(keyCaptor.capture(), anyString(),
                eq(15L), eq(TimeUnit.MINUTES));
        assertNotEquals(keyCaptor.getAllValues().get(0), keyCaptor.getAllValues().get(1));
    }

    @Test
    void shouldCreateDifferentKeysForSameUserDifferentIdempotencyKeys() throws Exception {
        when(redisDaoUtils.setIfAbsent(anyString(), anyString(), eq(15L), eq(TimeUnit.MINUTES)))
                .thenReturn(true);

        service.begin(USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE,
                "synthetic-idempotency-key-1", 50, DEVICE_ID);
        service.begin(USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE,
                "synthetic-idempotency-key-2", 50, DEVICE_ID);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisDaoUtils, org.mockito.Mockito.times(2)).setIfAbsent(keyCaptor.capture(), anyString(),
                eq(15L), eq(TimeUnit.MINUTES));
        assertNotEquals(keyCaptor.getAllValues().get(0), keyCaptor.getAllValues().get(1));
    }

    @Test
    void shouldFailClosedWhenRedisSetIfAbsentFails() {
        when(redisDaoUtils.setIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES)))
                .thenThrow(new IllegalStateException("synthetic redis failure"));

        ChargeIdempotencyException exception = assertThrows(ChargeIdempotencyException.class,
                () -> service.begin(USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE,
                        IDEMPOTENCY_KEY, 50, DEVICE_ID));

        assertEquals(ErrorConstantUtils.CHARGE_IDEMPOTENCY_UNAVAILABLE, exception.getCode());
    }

    @Test
    void shouldStoreSuccessWithoutFullChargeResponseOrCookies() throws Exception {
        ChargeIdempotencyService.ChargeIdempotencyContext context =
                new ChargeIdempotencyService.ChargeIdempotencyContext("redis-key", "fingerprint");

        service.markSuccess(context, "synthetic-order-id", "PAYMENT_SESSION_CREATED");

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisDaoUtils).set(eq("redis-key"), valueCaptor.capture());
        verify(redisDaoUtils).expire(eq("redis-key"), eq(60L), eq(TimeUnit.MINUTES));
        String stored = valueCaptor.getValue();
        assertTrue(stored.contains("SUCCESS"));
        ChargeIdempotencyService.ChargeIdempotencyRecord record = objectMapper.readValue(stored,
                ChargeIdempotencyService.ChargeIdempotencyRecord.class);
        assertEquals("synthetic-order-id", record.getOrderId());
        assertEquals("PAYMENT_SESSION_CREATED", record.getOrderStatus());
        assertFalse(stored.contains("password"));
        assertFalse(stored.contains("token"));
        assertFalse(stored.contains("session"));
        assertFalse(stored.contains("cookie"));
        assertFalse(stored.contains("alipay"));
    }

    private ChargeIdempotencyService.ChargeIdempotencyContext createInitialContext(int amount) throws Exception {
        when(redisDaoUtils.setIfAbsent(anyString(), anyString(), eq(15L), eq(TimeUnit.MINUTES)))
                .thenReturn(true);
        ChargeIdempotencyService.ChargeIdempotencyContext context = service.begin(USERNAME,
                ChargeIdempotencyService.ENDPOINT_CARD_CHARGE, IDEMPOTENCY_KEY, amount, DEVICE_ID);
        org.mockito.Mockito.reset(redisDaoUtils);
        return context;
    }

    private String recordJson(String status, String fingerprint) throws Exception {
        return objectMapper.writeValueAsString(new ChargeIdempotencyService.ChargeIdempotencyRecord(
                status, fingerprint, 1L, 2L));
    }
}
