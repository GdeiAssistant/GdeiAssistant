package cn.gdeiassistant.common.exceptionhandler;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.gdeiassistant.common.exception.verificationexception.SendEmailException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionLoggingPrivacyTest {
    @Test void exceptionMessagesAndCausesNeverEnterGlobalHandlerLogs() {
        var handler = new GlobalRestExceptionHandler();
        var logger = (Logger) org.slf4j.LoggerFactory.getLogger(GlobalRestExceptionHandler.class);
        var logs = new ListAppender<ILoggingEvent>(); logs.start(); logger.addAppender(logs);
        try {
            var request = new MockHttpServletRequest();
            var failure = new IllegalStateException("synthetic-private-message", new RuntimeException("synthetic-private-cause"));
            var response = handler.handleException(failure, request);
            assertEquals(500, response.getStatusCode().value());
            assertFalse(response.getBody().isSuccess());
            handler.handleVerificationException(new SendEmailException("synthetic-private-mail"), request);
            String text = logs.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a,b)->a+b);
            assertTrue(text.contains("IllegalStateException")); assertTrue(text.contains("ExceptionLoggingPrivacyTest"));
            assertTrue(text.contains("SendEmailException"));
            assertFalse(text.contains("synthetic-private"));
            assertTrue(logs.list.stream().allMatch(event -> event.getThrowableProxy() == null));
        } finally { logger.detachAppender(logs); logs.stop(); }
    }
}
