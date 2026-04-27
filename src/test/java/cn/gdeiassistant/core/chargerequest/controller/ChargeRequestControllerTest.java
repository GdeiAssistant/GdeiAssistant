package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.ChargeException.AmountNotAvailableException;
import cn.gdeiassistant.common.exception.ChargeException.ChargeIdempotencyException;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.interceptor.ApiAuthInterceptor;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderStatus;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeVO;
import cn.gdeiassistant.core.charge.service.ChargeIdempotencyService;
import cn.gdeiassistant.core.charge.service.ChargeOrderService;
import cn.gdeiassistant.core.charge.service.ChargeService;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChargeRequestControllerTest {

    private static final String SESSION_ID = "synthetic-session-for-charge-test";
    private static final String DEVICE_ID = "synthetic-device-for-charge-test";
    private static final String USERNAME = "synthetic_charge_user";
    private static final String PASSWORD = "synthetic-charge-password";
    private static final String IDEMPOTENCY_KEY = "synthetic-idempotency-key";
    private static final String IDEMPOTENCY_KEY_HASH = "synthetic-idempotency-key-hash";
    private static final String FINGERPRINT = "synthetic-fingerprint";
    private static final String ORDER_ID = "synthetic-order-id";

    @Mock
    private ChargeService chargeService;

    @Mock
    private ChargeIdempotencyService chargeIdempotencyService;

    @Mock
    private ChargeOrderService chargeOrderService;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private UserMapper userMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChargeRequestController controller = new ChargeRequestController();
        ReflectionTestUtils.setField(controller, "chargeService", chargeService);
        ReflectionTestUtils.setField(controller, "chargeIdempotencyService", chargeIdempotencyService);
        ReflectionTestUtils.setField(controller, "chargeOrderService", chargeOrderService);
        ReflectionTestUtils.setField(controller, "userCertificateService", userCertificateService);
        ReflectionTestUtils.setField(controller, "userMapper", userMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new ApiAuthInterceptor(List.of()))
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void shouldAllowChargeRequestWithoutClientHmacFieldsOrValidateTokenConfig() throws Exception {
        mockSuccessfulChargeWithoutIdempotency(50);

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.alipayURL").value("https://pay.example.invalid/synthetic-charge"))
                .andExpect(jsonPath("$.data.orderId").value(ORDER_ID))
                .andExpect(jsonPath("$.data.status").value(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name()))
                .andExpect(jsonPath("$.data.message").value("支付请求已生成，请完成支付并刷新余额。"));

        verify(chargeService).ChargeRequest(SESSION_ID, 50);
        verify(chargeService).SaveChargeLog(SESSION_ID, 50);
        verify(chargeOrderService).markProcessing(ORDER_ID);
        verify(chargeOrderService).markPaymentSessionCreated(ORDER_ID,
                "https://pay.example.invalid/synthetic-charge");
        verifyNoInteractions(chargeIdempotencyService);
    }

    @Test
    void shouldIgnoreLegacyHmacFieldsWhenTheyArePresent() throws Exception {
        mockSuccessfulChargeWithoutIdempotency(30);

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .param("amount", "30")
                        .param("password", PASSWORD)
                        .param("hmac", "synthetic-legacy-invalid-hmac")
                        .param("timestamp", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(chargeService).ChargeRequest(SESSION_ID, 30);
        verify(chargeService).SaveChargeLog(SESSION_ID, 30);
        verify(chargeOrderService).markProcessing(ORDER_ID);
        verify(chargeOrderService).markPaymentSessionCreated(ORDER_ID,
                "https://pay.example.invalid/synthetic-charge");
        verifyNoInteractions(chargeIdempotencyService);
    }

    @Test
    void shouldProcessFirstRequestWithIdempotencyKeyOnce() throws Exception {
        mockSuccessfulChargeWithIdempotency(50, IDEMPOTENCY_KEY, IDEMPOTENCY_KEY_HASH, FINGERPRINT, ORDER_ID);
        ChargeIdempotencyService.ChargeIdempotencyContext context =
                new ChargeIdempotencyService.ChargeIdempotencyContext("redis-key", FINGERPRINT);
        when(chargeIdempotencyService.begin(eq(USERNAME), eq(ChargeIdempotencyService.ENDPOINT_CARD_CHARGE),
                eq(IDEMPOTENCY_KEY), eq(50), eq(DEVICE_ID))).thenReturn(context);

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(chargeIdempotencyService).begin(USERNAME, ChargeIdempotencyService.ENDPOINT_CARD_CHARGE,
                IDEMPOTENCY_KEY, 50, DEVICE_ID);
        verify(chargeService).ChargeRequest(SESSION_ID, 50);
        verify(chargeService).SaveChargeLog(SESSION_ID, 50);
        verify(chargeOrderService).findByIdempotency(USERNAME, IDEMPOTENCY_KEY_HASH, FINGERPRINT);
        verify(chargeOrderService).markProcessing(ORDER_ID);
        verify(chargeOrderService).markPaymentSessionCreated(ORDER_ID,
                "https://pay.example.invalid/synthetic-charge");
        verify(chargeIdempotencyService).markProcessing(context, ORDER_ID, ChargeOrderStatus.PROCESSING.name());
        verify(chargeIdempotencyService).markSuccess(context, ORDER_ID,
                ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
    }

    @Test
    void shouldRejectDuplicateProcessingRequestWithoutCallingChargeService() throws Exception {
        mockAuthenticatedUser(PASSWORD);
        when(chargeIdempotencyService.begin(eq(USERNAME), eq(ChargeIdempotencyService.ENDPOINT_CARD_CHARGE),
                eq(IDEMPOTENCY_KEY), eq(50), eq(DEVICE_ID)))
                .thenThrow(new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT,
                        "相同充值请求正在处理中，请稍后查看结果"));

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT));

        verify(chargeService, never()).ChargeRequest(anyString(), anyInt());
        verify(chargeService, never()).SaveChargeLog(anyString(), anyInt());
        verifyNoInteractions(chargeOrderService);
    }

    @Test
    void shouldRejectDuplicateCompletedRequestWithoutCallingChargeService() throws Exception {
        mockAuthenticatedUser(PASSWORD);
        when(chargeIdempotencyService.begin(eq(USERNAME), eq(ChargeIdempotencyService.ENDPOINT_CARD_CHARGE),
                eq(IDEMPOTENCY_KEY), eq(50), eq(DEVICE_ID)))
                .thenThrow(new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT,
                        "相同充值请求已处理，请勿重复提交"));

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("相同充值请求已处理，请勿重复提交"));

        verify(chargeService, never()).ChargeRequest(anyString(), anyInt());
        verify(chargeService, never()).SaveChargeLog(anyString(), anyInt());
        verifyNoInteractions(chargeOrderService);
    }

    @Test
    void shouldRejectSameIdempotencyKeyWithDifferentAmount() throws Exception {
        mockAuthenticatedUser(PASSWORD);
        when(chargeIdempotencyService.begin(eq(USERNAME), eq(ChargeIdempotencyService.ENDPOINT_CARD_CHARGE),
                eq(IDEMPOTENCY_KEY), eq(60), eq(DEVICE_ID)))
                .thenThrow(new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT,
                        "同一幂等键不能用于不同充值参数"));

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .param("amount", "60")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("同一幂等键不能用于不同充值参数"));

        verify(chargeService, never()).ChargeRequest(anyString(), anyInt());
        verifyNoInteractions(chargeOrderService);
    }

    @Test
    void shouldAllowSameUserDifferentIdempotencyKeysToProcessSeparately() throws Exception {
        mockAuthenticatedUser(PASSWORD);
        ChargeVO charge = syntheticCharge();
        when(chargeService.ChargeRequest(SESSION_ID, 40)).thenReturn(charge);
        ChargeIdempotencyService.ChargeIdempotencyContext firstContext =
                new ChargeIdempotencyService.ChargeIdempotencyContext("redis-key-1", "fingerprint-1");
        ChargeIdempotencyService.ChargeIdempotencyContext secondContext =
                new ChargeIdempotencyService.ChargeIdempotencyContext("redis-key-2", "fingerprint-2");
        when(chargeIdempotencyService.begin(eq(USERNAME), eq(ChargeIdempotencyService.ENDPOINT_CARD_CHARGE),
                eq("synthetic-idempotency-key-1"), eq(40), eq(DEVICE_ID))).thenReturn(firstContext);
        when(chargeIdempotencyService.begin(eq(USERNAME), eq(ChargeIdempotencyService.ENDPOINT_CARD_CHARGE),
                eq("synthetic-idempotency-key-2"), eq(40), eq(DEVICE_ID))).thenReturn(secondContext);
        mockChargeOrderWithIdempotency(40, "synthetic-idempotency-key-1",
                "synthetic-idempotency-key-hash-1", "fingerprint-1", "synthetic-order-id-1");
        mockChargeOrderWithIdempotency(40, "synthetic-idempotency-key-2",
                "synthetic-idempotency-key-hash-2", "fingerprint-2", "synthetic-order-id-2");

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .header("Idempotency-Key", "synthetic-idempotency-key-1")
                        .param("amount", "40")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .header("Idempotency-Key", "synthetic-idempotency-key-2")
                        .param("amount", "40")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(chargeService, times(2)).ChargeRequest(SESSION_ID, 40);
        verify(chargeIdempotencyService).markSuccess(firstContext, "synthetic-order-id-1",
                ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
        verify(chargeIdempotencyService).markSuccess(secondContext, "synthetic-order-id-2",
                ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
    }

    @Test
    void shouldRejectChargeWhenIdempotencyStoreIsUnavailable() throws Exception {
        mockAuthenticatedUser(PASSWORD);
        when(chargeIdempotencyService.begin(eq(USERNAME), eq(ChargeIdempotencyService.ENDPOINT_CARD_CHARGE),
                eq(IDEMPOTENCY_KEY), eq(50), eq(DEVICE_ID)))
                .thenThrow(new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_UNAVAILABLE,
                        "充值幂等校验暂不可用，请稍后重试"));

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .header("X-Idempotency-Key", IDEMPOTENCY_KEY)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorConstantUtils.CHARGE_IDEMPOTENCY_UNAVAILABLE));

        verify(chargeService, never()).ChargeRequest(anyString(), anyInt());
        verifyNoInteractions(chargeOrderService);
    }

    @Test
    void shouldRejectChargeRequestWithoutLoginState() throws Exception {
        mockMvc.perform(post("/api/card/charge")
                        .header("X-Device-ID", DEVICE_ID)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userCertificateService, userMapper, chargeService);
    }

    @Test
    void shouldRejectChargeRequestWithoutDeviceHeader() throws Exception {
        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(userCertificateService, userMapper, chargeService);
    }

    @Test
    void shouldRejectChargeRequestWithWrongPassword() throws Exception {
        mockAuthenticatedUser("different-synthetic-password");

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verify(chargeService, never()).ChargeRequest(anyString(), anyInt());
        verify(chargeService, never()).SaveChargeLog(anyString(), anyInt());
        verifyNoInteractions(chargeIdempotencyService);
    }

    @Test
    void shouldRejectChargeRequestWithInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .param("amount", "0")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(userCertificateService, userMapper, chargeService);
    }

    @Test
    void shouldMarkOrderUnknownWhenExternalChargeStatusIsUnclear() throws Exception {
        mockAuthenticatedUser(PASSWORD);
        mockChargeOrderWithoutIdempotency(50, ORDER_ID);
        when(chargeService.ChargeRequest(SESSION_ID, 50))
                .thenThrow(new NetWorkTimeoutException("synthetic network timeout"));

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verify(chargeOrderService).markProcessing(ORDER_ID);
        verify(chargeOrderService).markUnknown(eq(ORDER_ID), any(NetWorkTimeoutException.class));
        verify(chargeService, never()).SaveChargeLog(anyString(), anyInt());
    }

    @Test
    void shouldMarkOrderFailedWhenExternalBusinessFailureIsClear() throws Exception {
        mockAuthenticatedUser(PASSWORD);
        mockChargeOrderWithoutIdempotency(50, ORDER_ID);
        when(chargeService.ChargeRequest(SESSION_ID, 50))
                .thenThrow(new AmountNotAvailableException("synthetic amount rejected"));

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verify(chargeOrderService).markProcessing(ORDER_ID);
        verify(chargeOrderService).markFailed(eq(ORDER_ID), any(AmountNotAvailableException.class));
        verify(chargeService, never()).SaveChargeLog(anyString(), anyInt());
    }

    @Test
    void shouldRejectDuplicateOrderFromDatabaseWithoutCallingChargeService() throws Exception {
        mockAuthenticatedUser(PASSWORD);
        ChargeIdempotencyService.ChargeIdempotencyContext context =
                new ChargeIdempotencyService.ChargeIdempotencyContext("redis-key", FINGERPRINT);
        when(chargeIdempotencyService.begin(eq(USERNAME), eq(ChargeIdempotencyService.ENDPOINT_CARD_CHARGE),
                eq(IDEMPOTENCY_KEY), eq(50), eq(DEVICE_ID))).thenReturn(context);
        ChargeOrderEntity existingOrder = syntheticOrder(ORDER_ID, 50);
        existingOrder.setStatus(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
        when(chargeOrderService.hashIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(IDEMPOTENCY_KEY_HASH);
        when(chargeOrderService.findByIdempotency(USERNAME, IDEMPOTENCY_KEY_HASH, FINGERPRINT))
                .thenReturn(existingOrder);

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT));

        verify(chargeIdempotencyService).markSuccess(context, ORDER_ID,
                ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
        verify(chargeService, never()).ChargeRequest(anyString(), anyInt());
        verify(chargeOrderService, never()).createOrder(anyString(), anyInt(), any(), any(), any(), any());
    }

    private void mockSuccessfulChargeWithoutIdempotency(int amount) throws Exception {
        mockAuthenticatedUser(PASSWORD);
        mockChargeOrderWithoutIdempotency(amount, ORDER_ID);
        when(chargeService.ChargeRequest(SESSION_ID, amount)).thenReturn(syntheticCharge());
    }

    private void mockSuccessfulChargeWithIdempotency(int amount, String rawIdempotencyKey,
                                                    String idempotencyKeyHash, String fingerprint,
                                                    String orderId) throws Exception {
        mockAuthenticatedUser(PASSWORD);
        mockChargeOrderWithIdempotency(amount, rawIdempotencyKey, idempotencyKeyHash, fingerprint, orderId);
        when(chargeService.ChargeRequest(SESSION_ID, amount)).thenReturn(syntheticCharge());
    }

    private void mockChargeOrderWithoutIdempotency(int amount, String orderId) throws Exception {
        when(chargeOrderService.buildPayloadFingerprint(amount, DEVICE_ID)).thenReturn(FINGERPRINT);
        ChargeOrderEntity order = syntheticOrder(orderId, amount);
        when(chargeOrderService.createOrder(eq(USERNAME), eq(amount), isNull(), eq(FINGERPRINT),
                isNull(), eq(DEVICE_ID))).thenReturn(order);
    }

    private void mockChargeOrderWithIdempotency(int amount, String rawIdempotencyKey,
                                               String idempotencyKeyHash, String fingerprint,
                                               String orderId) throws Exception {
        when(chargeOrderService.hashIdempotencyKey(rawIdempotencyKey)).thenReturn(idempotencyKeyHash);
        when(chargeOrderService.findByIdempotency(USERNAME, idempotencyKeyHash, fingerprint)).thenReturn(null);
        ChargeOrderEntity order = syntheticOrder(orderId, amount);
        when(chargeOrderService.createOrder(eq(USERNAME), eq(amount), eq(rawIdempotencyKey), eq(fingerprint),
                isNull(), eq(DEVICE_ID))).thenReturn(order);
    }

    private ChargeOrderEntity syntheticOrder(String orderId, int amount) {
        ChargeOrderEntity order = new ChargeOrderEntity();
        order.setOrderId(orderId);
        order.setUsername(USERNAME);
        order.setAmount(amount);
        order.setStatus(ChargeOrderStatus.CREATED.name());
        return order;
    }

    private ChargeVO syntheticCharge() {
        ChargeVO charge = new ChargeVO();
        charge.setAlipayURL("https://pay.example.invalid/synthetic-charge");
        return charge;
    }

    private void mockAuthenticatedUser(String storedPassword) {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(new User(USERNAME, storedPassword));
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(USERNAME);
        userEntity.setPassword(storedPassword);
        when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
    }
}
