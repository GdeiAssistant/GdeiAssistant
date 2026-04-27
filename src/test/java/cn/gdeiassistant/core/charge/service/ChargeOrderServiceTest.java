package cn.gdeiassistant.core.charge.service;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.core.charge.mapper.ChargeOrderMapper;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderStatus;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeOrderVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private static final String ORDER_ID = "synthetic-order-id";

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

    @Test
    void shouldQueryOwnOrderAsSafeVO() throws Exception {
        ChargeOrderEntity order = syntheticOrder(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
        order.setIdempotencyKeyHash("synthetic-idempotency-hash");
        order.setPayloadFingerprint("synthetic-payload-fingerprint");
        order.setDeviceIdHash("synthetic-device-hash");
        order.setPaymentUrlHash("synthetic-payment-url-hash");
        order.setErrorMessageSanitized("synthetic sanitized internal error");
        order.setManualReviewNote("synthetic manual note");
        when(chargeOrderMapper.findByOrderIdAndUsername(ORDER_ID, USERNAME)).thenReturn(order);

        ChargeOrderVO vo = service.queryOrder(ORDER_ID, USERNAME);
        String json = new ObjectMapper().writeValueAsString(vo);

        assertEquals(ORDER_ID, vo.getOrderId());
        assertEquals(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name(), vo.getStatus());
        assertEquals("支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。", vo.getMessage());
        assertFalse(json.contains("idempotencyKeyHash"));
        assertFalse(json.contains("payloadFingerprint"));
        assertFalse(json.contains("deviceIdHash"));
        assertFalse(json.contains("paymentUrlHash"));
        assertFalse(json.contains("errorMessageSanitized"));
        assertFalse(json.contains("manualReviewNote"));
        assertFalse(json.contains("最终到账成功"));
    }

    @Test
    void shouldRejectMissingOrOtherUserOrderWithSameSafeError() {
        when(chargeOrderMapper.findByOrderIdAndUsername(ORDER_ID, USERNAME)).thenReturn(null);

        DataNotExistException exception = assertThrows(DataNotExistException.class,
                () -> service.queryOrder(ORDER_ID, USERNAME));

        assertEquals("充值订单不存在或无权访问", exception.getMessage());
    }

    @Test
    void shouldQueryRecentOrdersWithStatusFilterInDescendingOrder() {
        ChargeOrderEntity newer = syntheticOrder(ChargeOrderStatus.UNKNOWN.name());
        newer.setOrderId("synthetic-newer-order-id");
        ChargeOrderEntity older = syntheticOrder(ChargeOrderStatus.UNKNOWN.name());
        older.setOrderId("synthetic-older-order-id");
        when(chargeOrderMapper.findRecentByUsernameAndStatus(USERNAME, ChargeOrderStatus.UNKNOWN.name(), 0, 20))
                .thenReturn(List.of(newer, older));

        List<ChargeOrderVO> orders = service.queryRecentOrders(USERNAME, 0, 20, "unknown");

        assertEquals("synthetic-newer-order-id", orders.get(0).getOrderId());
        assertEquals("synthetic-older-order-id", orders.get(1).getOrderId());
        assertEquals("充值状态暂无法确认，请稍后查看订单状态，避免重复提交。", orders.get(0).getMessage());
        assertEquals(60, orders.get(0).getRetryAfter());
        verify(chargeOrderMapper).findRecentByUsernameAndStatus(USERNAME, ChargeOrderStatus.UNKNOWN.name(), 0, 20);
    }

    @Test
    void shouldQueryRecentOrdersWithoutStatusFilterAndCapPageSize() {
        when(chargeOrderMapper.findRecentByUsername(USERNAME, 50, 50)).thenReturn(List.of());

        List<ChargeOrderVO> orders = service.queryRecentOrders(USERNAME, 1, 100, null);

        assertTrue(orders.isEmpty());
        verify(chargeOrderMapper).findRecentByUsername(USERNAME, 50, 50);
    }

    @Test
    void shouldRejectInvalidStatusFilter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.queryRecentOrders(USERNAME, 0, 20, "not-a-status"));

        assertEquals("请求参数不合法", exception.getMessage());
    }

    @Test
    void shouldUseSafeMessagesForUnknownAndManualReview() throws Exception {
        ChargeOrderEntity unknown = syntheticOrder(ChargeOrderStatus.UNKNOWN.name());
        unknown.setUnknownReason("synthetic password token session cookie reason");
        ChargeOrderVO unknownVO = service.toSafeVO(unknown);

        ChargeOrderEntity manualReview = syntheticOrder(ChargeOrderStatus.MANUAL_REVIEW.name());
        manualReview.setManualReviewNote("synthetic password token session cookie note");
        ChargeOrderVO manualReviewVO = service.toSafeVO(manualReview);

        String json = new ObjectMapper().writeValueAsString(List.of(unknownVO, manualReviewVO));
        assertTrue(unknownVO.getMessage().contains("暂无法确认"));
        assertTrue(manualReviewVO.getMessage().contains("需要进一步核实"));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("token"));
        assertFalse(json.contains("session"));
        assertFalse(json.contains("cookie"));
    }

    private ChargeOrderEntity syntheticOrder(String status) {
        ChargeOrderEntity order = new ChargeOrderEntity();
        order.setOrderId(ORDER_ID);
        order.setUsername(USERNAME);
        order.setAmount(50);
        order.setStatus(status);
        order.setCreatedAt(new Date(1000L));
        order.setUpdatedAt(new Date(2000L));
        order.setSubmittedAt(new Date(3000L));
        order.setCompletedAt(new Date(4000L));
        return order;
    }
}
