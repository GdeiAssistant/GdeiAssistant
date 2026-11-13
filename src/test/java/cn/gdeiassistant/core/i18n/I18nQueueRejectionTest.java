package cn.gdeiassistant.core.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
class I18nQueueRejectionTest {
    @Test void rejectedWorkCanBeSubmittedAgainAndNeverBreaksTheResponse(){
        var config=new I18nConfig("k","https://translation.example.invalid",7);
        var attempts=new AtomicInteger();
        var service=new I18nTranslationService(config, null, (Executor)r->{attempts.incrementAndGet();throw new RejectedExecutionException();});
        assertDoesNotThrow(()->service.enqueueTranslation("synthetic","en"));assertDoesNotThrow(()->service.enqueueTranslation("synthetic","en"));assertEquals(2,attempts.get());assertTrue(((java.util.Map<?,?>)ReflectionTestUtils.getField(service,"inFlight")).isEmpty());
    }
    @Test void absentExecutorDoesNotRetainWork(){
        var service=new I18nTranslationService(new I18nConfig("k","https://translation.example.invalid",7), null, null);
        service.enqueueTranslation("synthetic","en");
        assertTrue(((java.util.Map<?,?>)ReflectionTestUtils.getField(service,"inFlight")).isEmpty());
    }
}
