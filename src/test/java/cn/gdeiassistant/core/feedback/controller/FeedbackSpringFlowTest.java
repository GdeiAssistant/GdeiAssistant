package cn.gdeiassistant.core.feedback.controller;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.springutils.EmailUtils;
import cn.gdeiassistant.core.feedback.mapper.FeedbackMapper;
import cn.gdeiassistant.core.feedback.service.FeedbackService;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Real Spring async infrastructure stays enabled; SMTP failures must reach the HTTP response. */
class FeedbackSpringFlowTest {
    private AnnotationConfigApplicationContext context;
    private JavaMailSender transport;
    private MockMvc http;

    @Configuration @EnableAsync
    static class TestConfig {
        @Bean FeedbackController controller(){return new FeedbackController();}
        @Bean FeedbackService service(){return new FeedbackService();}
        @Bean EmailUtils emailUtils(){return new EmailUtils();}
    }
    @BeforeEach void setup(){
        context=new AnnotationConfigApplicationContext();
        // Prebuilt mocks are external collaborators, not Spring-managed implementations.
        transport=mock(JavaMailSender.class);
        var certificates=mock(UserCertificateService.class);
        when(certificates.getUserLoginCertificate("synthetic-session")).thenReturn(new User("synthetic-owner"));
        context.getBeanFactory().registerSingleton("transport",transport);
        context.getBeanFactory().registerSingleton("certificates",certificates);
        context.getBeanFactory().registerSingleton("feedbackMapper",mock(FeedbackMapper.class));
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("synthetic",Map.of(
                "email.from","sender@example.invalid","email.feedback","feedback@example.invalid","email.ticket","ticket@example.invalid")));
        context.register(TestConfig.class);context.refresh();transport=context.getBean(JavaMailSender.class);
        when(transport.createMimeMessage()).thenAnswer(call->new MimeMessage(Session.getInstance(new Properties())));
        http=MockMvcBuilders.standaloneSetup(context.getBean(FeedbackController.class)).setControllerAdvice(new GlobalRestExceptionHandler()).build();
    }
    @AfterEach void close(){context.close();}
    @Test void smtpFailureCannotReturnSuccessOrAutomaticallyResend() throws Exception {
        doThrow(new MailSendException("synthetic failure")).when(transport).send(any(MimeMessage.class));
        for(String route:new String[]{"function","ticket"}) {
            http.perform(multipart("/api/feedback/"+route).requestAttr("sessionId","synthetic-session").param("content","synthetic").param("type","network"))
                    .andExpect(jsonPath("$.success").value(false));
        }
        verify(transport,times(2)).send(any(MimeMessage.class));
    }
    @Test void disabledMailIsReportedBeforeReturningEvenWithAsyncEnabled() throws Exception {
        context.getBean(FeedbackService.class).setTicketEmail("");
        http.perform(multipart("/api/feedback/ticket").requestAttr("sessionId","synthetic-session").param("content","synthetic").param("type","network"))
                .andExpect(jsonPath("$.success").value(false));verifyNoInteractions(transport);
    }
    @Test void successfulResponseWaitsForTransportAcceptanceOnRequestThread() throws Exception {
        var sendingThread=new AtomicReference<Thread>();
        doAnswer(call->{sendingThread.set(Thread.currentThread());return null;}).when(transport).send(any(MimeMessage.class));
        http.perform(multipart("/api/feedback/function").requestAttr("sessionId","synthetic-session").param("content","synthetic"))
                .andExpect(jsonPath("$.success").value(true));
        assertSame(Thread.currentThread(),sendingThread.get());verify(transport).send(any(MimeMessage.class));
    }
}
