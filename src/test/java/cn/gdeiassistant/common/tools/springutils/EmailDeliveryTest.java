package cn.gdeiassistant.common.tools.springutils;

import cn.gdeiassistant.common.exception.verificationexception.SendEmailException;
import cn.gdeiassistant.core.capability.impl.SmtpEmailVerificationSender;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailDeliveryTest {
    @Test void missingMailTransportCannotReportSuccessfulDelivery() {
        EmailUtils utility = new EmailUtils();
        assertThrows(MessagingException.class, () -> utility.sendEmail("sender@example.invalid", "recipient@example.invalid", "synthetic", "body", new byte[0][]));
    }
    @Test void realMimeMessageKeepsSenderRecipientUtf8BodyAndAttachment() throws Exception {
        JavaMailSender transport = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(transport.createMimeMessage()).thenReturn(message);
        EmailUtils utility = new EmailUtils(); ReflectionTestUtils.setField(utility, "javaMailSender", transport);
        utility.sendEmail("sender@example.invalid", "recipient@example.invalid", "合成邮件", "验证码测试", new byte[][]{new byte[]{1,2,3}});
        message.saveChanges(); verify(transport).send(message);
        assertEquals("sender@example.invalid", message.getFrom()[0].toString());
        assertEquals("recipient@example.invalid", message.getAllRecipients()[0].toString()); assertEquals("合成邮件", message.getSubject());
        var multipart = (jakarta.mail.Multipart) message.getContent();
        var mixed = (jakarta.mail.Multipart) multipart.getBodyPart(0).getContent();
        assertTrue(mixed.getBodyPart(0).getContent().toString().contains("验证码测试"));
        assertEquals("attachment-image-1.jpg", multipart.getBodyPart(1).getFileName());
        assertArrayEquals(new byte[]{1,2,3}, multipart.getBodyPart(1).getInputStream().readAllBytes());
    }
    @Test void smtpTransportFailureBecomesDeliveryFailureAndDoesNotRetry() throws Exception {
        JavaMailSender transport = mock(JavaMailSender.class);
        when(transport.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("synthetic outage")).when(transport).send(any(MimeMessage.class));
        EmailUtils utility = new EmailUtils(); ReflectionTestUtils.setField(utility, "javaMailSender", transport);
        var sender = new SmtpEmailVerificationSender(); ReflectionTestUtils.setField(sender, "emailUtils", utility);
        sender.setSmtpHost("synthetic.invalid"); sender.setSenderEmail("sender@example.invalid"); sender.setSmtpPassword("synthetic-only");
        assertThrows(SendEmailException.class, () -> sender.sendVerificationCode("recipient@example.invalid", 123456));
        verify(transport, times(1)).send(any(MimeMessage.class));
    }
    @Test void unconfiguredSmtpNeverInvokesMailUtility() throws Exception {
        EmailUtils utility = mock(EmailUtils.class); var sender = new SmtpEmailVerificationSender();
        ReflectionTestUtils.setField(sender, "emailUtils", utility);
        assertThrows(SendEmailException.class, () -> sender.sendVerificationCode("recipient@example.invalid", 123456));
        verifyNoInteractions(utility);
    }
}
