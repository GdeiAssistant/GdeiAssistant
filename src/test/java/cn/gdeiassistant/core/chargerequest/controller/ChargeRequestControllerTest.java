package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.interceptor.ApiAuthInterceptor;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeVO;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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

    @Mock
    private ChargeService chargeService;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private UserMapper userMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChargeRequestController controller = new ChargeRequestController();
        ReflectionTestUtils.setField(controller, "chargeService", chargeService);
        ReflectionTestUtils.setField(controller, "userCertificateService", userCertificateService);
        ReflectionTestUtils.setField(controller, "userMapper", userMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new ApiAuthInterceptor(List.of()))
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void shouldAllowChargeRequestWithoutClientHmacFieldsOrValidateTokenConfig() throws Exception {
        mockSuccessfulCharge(50);

        mockMvc.perform(post("/api/card/charge")
                        .requestAttr("sessionId", SESSION_ID)
                        .header("X-Device-ID", DEVICE_ID)
                        .param("amount", "50")
                        .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.alipayURL").value("https://pay.example.invalid/synthetic-charge"));

        verify(chargeService).ChargeRequest(SESSION_ID, 50);
        verify(chargeService).SaveChargeLog(SESSION_ID, 50);
    }

    @Test
    void shouldIgnoreLegacyHmacFieldsWhenTheyArePresent() throws Exception {
        mockSuccessfulCharge(30);

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

    private void mockSuccessfulCharge(int amount) throws Exception {
        mockAuthenticatedUser(PASSWORD);
        ChargeVO charge = new ChargeVO();
        charge.setAlipayURL("https://pay.example.invalid/synthetic-charge");
        when(chargeService.ChargeRequest(SESSION_ID, amount)).thenReturn(charge);
    }

    private void mockAuthenticatedUser(String storedPassword) {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(new User(USERNAME, storedPassword));
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(USERNAME);
        userEntity.setPassword(storedPassword);
        when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
    }
}
