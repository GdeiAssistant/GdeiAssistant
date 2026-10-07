package cn.gdeiassistant.core.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
class I18nQueueRejectionTest {
    @Test void rejectedWorkCanBeSubmittedAgainAndNeverBreaksTheResponse(){var service=new I18nTranslationService(new I18nConfig());var attempts=new AtomicInteger();ReflectionTestUtils.setField(service,"i18nExecutor",(Executor)r->{attempts.incrementAndGet();throw new RejectedExecutionException();});
        assertDoesNotThrow(()->service.enqueueTranslation("synthetic","en"));assertDoesNotThrow(()->service.enqueueTranslation("synthetic","en"));assertEquals(2,attempts.get());assertTrue(((java.util.Map<?,?>)ReflectionTestUtils.getField(service,"inFlight")).isEmpty());}
    @Test void absentExecutorDoesNotRetainWork(){var service=new I18nTranslationService(new I18nConfig());service.enqueueTranslation("synthetic","en");assertTrue(((java.util.Map<?,?>)ReflectionTestUtils.getField(service,"inFlight")).isEmpty());}
}
