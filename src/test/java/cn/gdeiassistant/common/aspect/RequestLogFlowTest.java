package cn.gdeiassistant.common.aspect;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.gdeiassistant.common.constant.ObservabilityConstants;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestLogFlowTest {
    private final RequestLogAspect aspect=new RequestLogAspect();
    private final ProceedingJoinPoint point=mock(ProceedingJoinPoint.class);
    private final CodeSignature signature=mock(CodeSignature.class);
    private final SimpleMeterRegistry registry=new SimpleMeterRegistry();
    private final ListAppender<ILoggingEvent> logs=new ListAppender<>();
    private final Logger logger=(Logger)org.slf4j.LoggerFactory.getLogger(RequestLogAspect.class);
    @BeforeEach void setup() {
        var settings=new ObservabilityConstants();ReflectionTestUtils.setField(settings,"slowRequestThresholdMs",-1L);
        ReflectionTestUtils.setField(aspect,"observabilityConstants",settings);ReflectionTestUtils.setField(aspect,"meterRegistry",registry);
        when(point.getSignature()).thenReturn(signature);when(signature.getDeclaringType()).thenReturn(getClass());
        when(signature.getDeclaringTypeName()).thenReturn(getClass().getName());when(signature.getName()).thenReturn("handle");
        logs.start();logger.addAppender(logs);
    }
    @AfterEach void cleanup() {logger.detachAppender(logs);logs.stop();registry.close();RequestContextHolder.resetRequestAttributes();}
    private String logText() {return logs.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("",(a,b)->a+'\n'+b);}
    private void args(Object[] args,String... names) {when(point.getArgs()).thenReturn(args);when(signature.getParameterNames()).thenReturn(names);}
    @Test void successfulRequestsRedactNestedSecretsSkipBinaryAndBoundMetricLabels() throws Throwable {
        var request=new MockHttpServletRequest("POST","/api/synthetic");request.setAttribute("requestId","synthetic-rid");
        request.addHeader("X-Forwarded-For","192.0.2.1, 192.0.2.2");
        args(new Object[]{request,Map.of("password","synthetic-password","list",List.of(Map.of("token","synthetic-token","ok","kept"))),"synthetic-secret",new MockMultipartFile("file",new byte[]{1}),"合成说明 ".repeat(120)},"request","payload","password","file","description");
        when(point.proceed()).thenReturn("result");assertEquals("result",aspect.logRequestCorrelation(point));
        var text=logText();assertTrue(text.contains("rid:synthetic-rid"));assertTrue(text.contains("ip:192.0.2.1"));
        assertTrue(text.contains("status:OK"));assertTrue(text.contains("kept"));assertTrue(text.contains("[truncated]"));
        assertFalse(text.contains("synthetic-password"));assertFalse(text.contains("synthetic-token"));assertFalse(text.contains("synthetic-secret"));
        assertEquals(1,registry.get("http.requests.slow").tags("method","POST","handler","RequestLogFlowTest.handle").counter().count());
        assertFalse(registry.get("http.requests.slow").counter().getId().getTags().stream().anyMatch(t->t.getKey().equals("path")));
    }
    @Test void failureStillLogsAndPropagatesTheOriginalExceptionUsingRequestContextFallback() throws Throwable {
        var request=new MockHttpServletRequest("GET","/api/synthetic");request.addHeader("X-Real-IP","192.0.2.3");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));args(new Object[]{null,4},"empty","size");
        var failure=new IllegalStateException("synthetic failure");when(point.proceed()).thenThrow(failure);
        assertSame(failure,assertThrows(IllegalStateException.class,()->aspect.logRequestCorrelation(point)));
        assertTrue(logText().contains("status:FAIL"));assertTrue(logText().contains("ip:192.0.2.3"));
    }
    @Test void missingRequestAndMissingParameterMetadataDoNotBreakTheHandler() throws Throwable {
        args(new Object[]{"value"},"value");when(signature.getParameterNames()).thenThrow(new IllegalStateException("no metadata"));
        when(point.proceed()).thenReturn(7);assertEquals(7,aspect.logRequestCorrelation(point));assertTrue(logText().contains("rid:?"));
    }
    @Test void legacyPersistenceUsesTheSameNestedRedactionAndExcludesUploads() {
        var request=new MockHttpServletRequest();request.addHeader("User-Agent","synthetic-agent");
        args(new Object[]{request,Map.of("password","synthetic-password","nested",List.of(Map.of("email","synthetic@example.invalid"))),null,new MockMultipartFile("file",new byte[]{1})},"request","payload","empty","file");
        aspect.restSaveQueryLog(point);assertTrue(logText().contains("RequestLog -"));assertTrue(logText().contains("synthetic-agent"));
        assertFalse(logText().contains("synthetic-password"));assertFalse(logText().contains("synthetic@example.invalid"));
    }
}
