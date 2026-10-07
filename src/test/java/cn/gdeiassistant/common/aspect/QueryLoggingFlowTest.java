package cn.gdeiassistant.common.aspect;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.gdeiassistant.common.constant.ObservabilityConstants;
import cn.gdeiassistant.common.pojo.entity.User;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class QueryLoggingFlowTest {
    @Test void logsQueryCategoriesWithoutExposingCampusIdentityAndRecordsSlowQueries() throws Throwable {
        var aspect=new QueryLogAspect();var settings=new ObservabilityConstants();ReflectionTestUtils.setField(settings,"slowQueryThresholdMs",-1L);
        var registry=new SimpleMeterRegistry();ReflectionTestUtils.setField(aspect,"observabilityConstants",settings);ReflectionTestUtils.setField(aspect,"meterRegistry",registry);
        var call=mock(ProceedingJoinPoint.class);var signature=mock(Signature.class);when(call.getSignature()).thenReturn(signature);when(call.proceed()).thenReturn("synthetic-result");
        var request=new MockHttpServletRequest();request.setAttribute("user",new User("synthetic-owner-private"));request.getSession().setAttribute("username","synthetic-owner-private");when(call.getArgs()).thenReturn(new Object[]{request});
        var logs=new ListAppender<ILoggingEvent>();var logger=(Logger)org.slf4j.LoggerFactory.getLogger(QueryLogAspect.class);logs.start();logger.addAppender(logs);
        try {
            for(String name:new String[]{"gradequery","schedulequery","cardquery","cardInfoQuery","cardLost","querySpareRoomList","other"}) {
                when(signature.getName()).thenReturn(name);assertEquals("synthetic-result",aspect.saveQueryLog(call));assertEquals("synthetic-result",aspect.restSaveQueryLog(call));
            }
            when(signature.toShortString()).thenReturn("MarketplaceController.query()");assertEquals("synthetic-result",aspect.communityQueryLog(call));
            assertEquals(15,registry.getMeters().stream().mapToDouble(m->((io.micrometer.core.instrument.Counter)m).count()).sum());
            assertTrue(logs.list.stream().anyMatch(l->l.getFormattedMessage().contains("查询了成绩")));
            assertTrue(logs.list.stream().noneMatch(l->l.getFormattedMessage().contains("synthetic-owner-private")));
            ReflectionTestUtils.setField(settings,"slowQueryThresholdMs",Long.MAX_VALUE);assertEquals("synthetic-result",aspect.communityQueryLog(call));
            var failure=new IllegalStateException("synthetic failure");when(call.proceed()).thenThrow(failure);assertSame(failure,assertThrows(IllegalStateException.class,()->aspect.communityQueryLog(call)));
        } finally {logger.detachAppender(logs);logs.stop();registry.close();}
    }
}
