package cn.gdeiassistant.common.aspect;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.gdeiassistant.common.constant.ObservabilityConstants;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerMapping;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestLogAspectTest {
    @Test
    void rawPathAndUnknownHttpVerbCannotLeakOrCreateMetricLabels() throws Throwable {
        var aspect = new RequestLogAspect();
        var settings = new ObservabilityConstants();
        ReflectionTestUtils.setField(settings, "slowRequestThresholdMs", -1L);
        ReflectionTestUtils.setField(aspect, "observabilityConstants", settings);
        var registry = new SimpleMeterRegistry();
        try {
            ReflectionTestUtils.setField(aspect, "meterRegistry", registry);
            var point = mock(ProceedingJoinPoint.class);
            var signature = mock(CodeSignature.class);
            when(point.getSignature()).thenReturn(signature);
            when(signature.getDeclaringType()).thenReturn(getClass());
            when(signature.getName()).thenReturn("handle");
            var request = new MockHttpServletRequest("PRIVATE-custom-verb", "/api/search/private-keyword");
            request.setAttribute("requestId", "synthetic-id");
            request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/search/{keyword}");
            request.addHeader("Authorization", "synthetic-private-token");
            when(point.getArgs()).thenReturn(new Object[]{request, "private-body"});
            var logger = (Logger) org.slf4j.LoggerFactory.getLogger(RequestLogAspect.class);
            var logs = new ListAppender<ILoggingEvent>();
            logs.start(); logger.addAppender(logs);
            try {
                aspect.logRequestCorrelation(point);
                String text = logs.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a,b) -> a+b);
                assertTrue(text.contains("/api/search/{keyword}"));
                assertFalse(text.contains("private-keyword"));
                assertFalse(text.contains("PRIVATE-custom-verb"));
                assertFalse(text.contains("synthetic-private-token"));
                assertFalse(text.contains("private-body"));
                assertEquals(1, registry.get("http.requests.slow").tag("method", "?").counter().count());
            } finally {
                logger.detachAppender(logs); logs.stop();
            }
        } finally { registry.close(); }
    }
}
