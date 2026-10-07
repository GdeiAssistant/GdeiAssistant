package cn.gdeiassistant.core.email.service;

import cn.gdeiassistant.common.exception.verificationexception.*;
import cn.gdeiassistant.common.pojo.entity.Email;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.verificationcode.VerificationCodeDao;
import cn.gdeiassistant.core.capability.email.EmailVerificationSender;
import cn.gdeiassistant.core.email.controller.EmailController;
import cn.gdeiassistant.core.email.mapper.EmailMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Exercises controller -> service -> delivery/OTP/persistence boundaries without real mail. */
class EmailBindingFlowTest {
    private final VerificationCodeDao codes = mock(VerificationCodeDao.class);
    private final EmailVerificationSender sender = mock(EmailVerificationSender.class);
    private final EmailMapper mapper = mock(EmailMapper.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final EmailService service = new EmailService();
    private final EmailController controller = new EmailController();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private static final String EMAIL = "synthetic@example.invalid";

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "verificationCodeDao", codes);
        ReflectionTestUtils.setField(service, "emailVerificationSender", sender);
        ReflectionTestUtils.setField(service, "emailMapper", mapper);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        ReflectionTestUtils.setField(controller, "emailService", service);
        request.setAttribute("sessionId", "session");
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("owner"));
    }

    @Test void generatedCodeBindsOnceAndReplayCannotWrite() throws Exception {
        controller.getEmailVerificationCode(request, EMAIL);
        ArgumentCaptor<Integer> code = ArgumentCaptor.forClass(Integer.class);
        verify(codes).saveEmailVerificationCode(eq(EMAIL), code.capture());
        assertTrue(code.getValue() >= 100000 && code.getValue() <= 999999);
        verify(sender).sendVerificationCode(EMAIL, code.getValue());
        when(codes.consumeEmailVerificationCode(EMAIL, code.getValue())).thenReturn(true, false);
        assertTrue(controller.bindEmail(request, EMAIL, code.getValue()).isSuccess());
        assertThrows(VerificationCodeInvalidException.class,
                () -> controller.bindEmail(request, EMAIL, code.getValue()));
        verify(mapper, times(1)).insertEmail("owner", EMAIL);
    }

    @Test void sendFailureInvalidatesThatCodeAndPropagates() throws Exception {
        doThrow(new SendEmailException("synthetic failure")).when(sender).sendVerificationCode(eq(EMAIL), anyInt());
        assertThrows(SendEmailException.class, () -> controller.getEmailVerificationCode(request, EMAIL));
        ArgumentCaptor<Integer> code = ArgumentCaptor.forClass(Integer.class);
        verify(codes).saveEmailVerificationCode(eq(EMAIL), code.capture());
        verify(codes).consumeEmailVerificationCode(EMAIL, code.getValue());
        verifyNoInteractions(mapper);
    }

    @Test void incorrectCodeCannotQueryOrModifyBinding() {
        assertThrows(VerificationCodeInvalidException.class, () -> controller.bindEmail(request, EMAIL, 123456));
        verifyNoInteractions(mapper, certificates);
    }

    @Test void existingBindingIsUpdatedAndCanBeRemoved() throws Exception {
        Email existing = new Email(); existing.setEmail("old@example.invalid");
        when(mapper.selectEmail("owner")).thenReturn(existing);
        when(codes.consumeEmailVerificationCode(EMAIL, 123456)).thenReturn(true);
        controller.bindEmail(request, EMAIL, 123456);
        verify(mapper).updateEmail(existing); assertEquals(EMAIL, existing.getEmail());
        assertEquals(EMAIL, controller.queryEmailStatus(request).getData());
        assertTrue(controller.unBindEmail(request).isSuccess());
        verify(mapper).deleteEmail("owner"); verify(mapper, never()).insertEmail(anyString(), anyString());
    }

    @Test void absentBindingReturnsEmptyStatusAndDoesNotDelete() {
        assertNull(controller.queryEmailStatus(request).getData());
        assertFalse(controller.unBindEmail(request).isSuccess());
        service.unBindUserEmail("session");
        verify(mapper, never()).deleteEmail(anyString());
    }
}
