package cn.gdeiassistant.core.charge.service;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.core.charge.mapper.ChargeOrderMapper;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargeOrderServiceTest {

    private static final String USERNAME = "synthetic_charge_user";
    private static final String IDEMPOTENCY_KEY = "synthetic-idempotency-key";
    private static final String DEVICE_ID = "synthetic-device-id";
    private static final String FINGERPRINT = "synthetic-fingerprint";

    @Mock
    private ChargeOrderMapper chargeOrderMapper;

    private ChargeOrderService service;

    @BeforeEach
    void setUp() {
        service = new ChargeOrderService();
        ReflectionTestUtils.setField(service, "chargeOrderMapper", chargeOrderMapper);
        ReflectionTestUtils.setField(service, "chargeIdempotencyService", new ChargeIdempotencyService());
    }

    @Test
    void shouldCreateOrderWithoutRawSensitiveValues() throws Exception {
        ChargeOrderEntity created = service.createOrder(USERNAME, 50, IDEMPOTENCY_KEY, FINGERPRINT,
                "synthetic-request-id", DEVICE_ID);

        ArgumentCaptor<ChargeOrderEntity> orderCaptor = ArgumentCaptor.forClass(ChargeOrderEntity.class);
        verify(chargeOrderMapper).insertChargeOrder(orderCaptor.capture());
        ChargeOrderEntity stored = orderCaptor.getValue();

        assertEquals(created.getOrderId(), stored.getOrderId());
        assertEquals(USERNAME, stored.getUsername());
        assertEquals(50, stored.getAmount());
        assertEquals(ChargeOrderStatus.CREATED.name(), stored.getStatus());
        assertEquals(FINGERPRINT, stored.getPayloadFingerprint());
        assertEquals(64, stored.getIdempotencyKeyHash().length());
        assertEquals(64, stored.getDeviceIdHash().length());
        assertNotEquals(IDEMPOTENCY_KEY, stored.getIdempotencyKeyHash());
        assertNotEquals(DEVICE_ID, stored.getDeviceIdHash());
        assertFalse(stored.getIdempotencyKeyHash().contains(IDEMPOTENCY_KEY));
        assertFalse(stored.getDeviceIdHash().contains(DEVICE_ID));
        assertNull(stored.getPaymentUrlHash());
        assertNull(stored.getErrorMessageSanitized());
        assertEquals(0, stored.getCheckCount());
        assertEquals(0, stored.getVersion());
    }

    @Test
    void shouldMarkPaymentSessionCreatedWithOnlyPaymentUrlHash() {
        service.markPaymentSessionCreated("synthetic-order-id",
                "https://pay.example.invalid/synthetic-payment-session");

        ArgumentCaptor<String> paymentHashCaptor = ArgumentCaptor.forClass(String.class);
        verify(chargeOrderMapper).updateChargeOrderStatus(eq("synthetic-order-id"),
                eq(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name()), isNull(), any(Date.class),
                paymentHashCaptor.capture(), isNull(), isNull(), isNull());

        assertEquals(64, paymentHashCaptor.getValue().length());
        assertFalse(paymentHashCaptor.getValue().contains("pay.example.invalid"));
    }

    @Test
    void shouldStoreOnlySafeUnknownReason() {
        service.markUnknown("synthetic-order-id",
                new NetWorkTimeoutException("synthetic password token session cookie value"));

        verify(chargeOrderMapper).updateChargeOrderStatus(eq("synthetic-order-id"),
                eq(ChargeOrderStatus.UNKNOWN.name()), isNull(), isNull(), isNull(),
                eq("NetWorkTimeoutException"), eq("Charge status could not be confirmed"),
                eq("NetWorkTimeoutException"));
    }

    @Test
    void shouldLimitSafeOrderStatusToOwner() {
        ChargeOrderEntity order = new ChargeOrderEntity();
        order.setOrderId("synthetic-order-id");
        order.setUsername(USERNAME);
        when(chargeOrderMapper.findByOrderId("synthetic-order-id")).thenReturn(order);

        assertEquals(order, service.findSafeOrderStatus("synthetic-order-id", USERNAME));
        assertNull(service.findSafeOrderStatus("synthetic-order-id", "synthetic_other_user"));
    }

    @Test
    void shouldBuildStablePayloadFingerprintWithoutPassword() {
        String first = service.buildPayloadFingerprint(50, DEVICE_ID);
        String second = service.buildPayloadFingerprint(50, DEVICE_ID);

        assertEquals(first, second);
        assertEquals(64, first.length());
        assertFalse(first.contains("password"));
        assertFalse(first.contains(DEVICE_ID));
        assertTrue(first.matches("[0-9a-f]+"));
    }
}
