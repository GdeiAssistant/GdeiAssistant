package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.pojo.Entity.Email;
import cn.gdeiassistant.core.email.controller.EmailController;
import cn.gdeiassistant.core.email.service.EmailService;
import cn.gdeiassistant.core.phone.controller.PhoneController;
import cn.gdeiassistant.core.phone.pojo.vo.PhoneVO;
import cn.gdeiassistant.core.phone.service.PhoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContactValidationContractTest {

    private MockMvc mockMvc;
    private EmailService emailService;
    private PhoneService phoneService;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        phoneService = mock(PhoneService.class);

        EmailController emailController = new EmailController();
        ReflectionTestUtils.setField(emailController, "emailService", emailService);

        PhoneController phoneController = new PhoneController();
        ReflectionTestUtils.setField(phoneController, "phoneService", phoneService);

        mockMvc = MockMvcBuilders.standaloneSetup(emailController, phoneController)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void emailVerificationRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/email/verification")
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(emailService, phoneService);
    }

    @Test
    void bindEmailRejectsInvalidVerificationCodeBounds() throws Exception {
        mockMvc.perform(post("/api/email/bind")
                        .requestAttr("sessionId", "test-session")
                        .param("email", "student@example.com")
                        .param("randomCode", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(emailService, phoneService);
    }

    @Test
    void emailStatusReturnsBoundAddress() throws Exception {
        Email email = new Email();
        email.setEmail("student@example.com");
        when(emailService.queryUserEmail("test-session")).thenReturn(email);

        mockMvc.perform(get("/api/email/status")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("student@example.com"));
    }

    @Test
    void phoneVerificationRejectsInvalidPhone() throws Exception {
        mockMvc.perform(post("/api/phone/verification")
                        .param("code", "86")
                        .param("phone", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(emailService, phoneService);
    }

    @Test
    void phoneVerificationRejectsUnsupportedCodeBeforeService() throws Exception {
        mockMvc.perform(post("/api/phone/verification")
                        .param("code", "999")
                        .param("phone", "13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(emailService, phoneService);
    }

    @Test
    void phoneStatusReturnsBoundPhone() throws Exception {
        PhoneVO phone = new PhoneVO();
        phone.setUsername("testuser");
        phone.setPhone("138********");
        when(phoneService.queryUserPhone("test-session")).thenReturn(phone);

        mockMvc.perform(get("/api/phone/status")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.phone").value("138********"));
    }
}
