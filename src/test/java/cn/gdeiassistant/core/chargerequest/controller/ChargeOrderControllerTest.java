package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.interceptor.ApiAuthInterceptor;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderStatus;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeOrderVO;
import cn.gdeiassistant.core.charge.service.ChargeOrderService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChargeOrderControllerTest {

    private static final String SESSION_ID = "synthetic-session-for-charge-order-query";
    private static final String USERNAME = "synthetic_charge_user";
    private static final String ORDER_ID = "synthetic-order-id";

    @Mock
    private ChargeOrderService chargeOrderService;

    @Mock
    private UserCertificateService userCertificateService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChargeOrderController controller = new ChargeOrderController();
        ReflectionTestUtils.setField(controller, "chargeOrderService", chargeOrderService);
        ReflectionTestUtils.setField(controller, "userCertificateService", userCertificateService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new ApiAuthInterceptor(List.of()))
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void shouldQueryOwnChargeOrderDetail() throws Exception {
        mockAuthenticatedUser();
        ChargeOrderVO order = syntheticOrder(ORDER_ID, ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
        when(chargeOrderService.queryOrder(ORDER_ID, USERNAME)).thenReturn(order);

        mockMvc.perform(get("/api/card/charge/orders/{orderId}", ORDER_ID)
                        .requestAttr("sessionId", SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(ORDER_ID))
                .andExpect(jsonPath("$.data.amount").value(50))
                .andExpect(jsonPath("$.data.status").value(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name()))
                .andExpect(jsonPath("$.data.message").value("支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。"))
                .andExpect(jsonPath("$.data.idempotencyKeyHash").doesNotExist())
                .andExpect(jsonPath("$.data.payloadFingerprint").doesNotExist())
                .andExpect(jsonPath("$.data.deviceIdHash").doesNotExist())
                .andExpect(jsonPath("$.data.paymentUrlHash").doesNotExist())
                .andExpect(jsonPath("$.data.errorMessageSanitized").doesNotExist())
                .andExpect(jsonPath("$.data.manualReviewNote").doesNotExist());

        verify(chargeOrderService).queryOrder(ORDER_ID, USERNAME);
    }

    @Test
    void shouldReturnSafeNotFoundForMissingOrOtherUserOrder() throws Exception {
        mockAuthenticatedUser();
        when(chargeOrderService.queryOrder(ORDER_ID, USERNAME))
                .thenThrow(new DataNotExistException("充值订单不存在或无权访问"));

        mockMvc.perform(get("/api/card/charge/orders/{orderId}", ORDER_ID)
                        .requestAttr("sessionId", SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("充值订单不存在或无权访问"));
    }

    @Test
    void shouldQueryRecentChargeOrdersForCurrentUserOnly() throws Exception {
        mockAuthenticatedUser();
        ChargeOrderVO order = syntheticOrder(ORDER_ID, ChargeOrderStatus.PROCESSING.name());
        when(chargeOrderService.queryRecentOrders(USERNAME, 0, 20, ChargeOrderStatus.PROCESSING.name()))
                .thenReturn(List.of(order));

        mockMvc.perform(get("/api/card/charge/orders")
                        .requestAttr("sessionId", SESSION_ID)
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", ChargeOrderStatus.PROCESSING.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderId").value(ORDER_ID))
                .andExpect(jsonPath("$.data[0].status").value(ChargeOrderStatus.PROCESSING.name()))
                .andExpect(jsonPath("$.data[0].retryAfter").value(60))
                .andExpect(jsonPath("$.data[0].username").doesNotExist());

        verify(chargeOrderService).queryRecentOrders(USERNAME, 0, 20, ChargeOrderStatus.PROCESSING.name());
    }

    @Test
    void shouldRejectUnauthenticatedOrderQueries() throws Exception {
        mockMvc.perform(get("/api/card/charge/orders/{orderId}", ORDER_ID))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userCertificateService, chargeOrderService);
    }

    private void mockAuthenticatedUser() {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(new User(USERNAME, "synthetic"));
    }

    private ChargeOrderVO syntheticOrder(String orderId, String status) {
        ChargeOrderVO order = new ChargeOrderVO();
        order.setOrderId(orderId);
        order.setAmount(50);
        order.setStatus(status);
        if (ChargeOrderStatus.PAYMENT_SESSION_CREATED.name().equals(status)) {
            order.setMessage("支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。");
        } else {
            order.setMessage("充值请求正在处理中，请稍后查看结果。");
            order.setRetryAfter(60);
        }
        return order;
    }
}
