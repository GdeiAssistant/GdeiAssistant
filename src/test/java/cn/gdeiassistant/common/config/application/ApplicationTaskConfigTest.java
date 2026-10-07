package cn.gdeiassistant.common.config.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationTaskConfigTest {
    @AfterEach void cleanup() { MDC.clear(); }

    @Test void eachApplicationExecutorPropagatesOnlyRequestId() throws Exception {
        var config = new ApplicationTaskConfig();
        var provider = config.providerExecutor(); provider.initialize();
        var executors = new ThreadPoolTaskExecutor[]{(ThreadPoolTaskExecutor) config.asyncExecutor(),
                provider, (ThreadPoolTaskExecutor) config.campusSyncExecutor()};
        try {
            for (var executor : executors) {
                MDC.put("requestId", "synthetic-request"); MDC.put("private-entry", "synthetic-private");
                assertEquals("synthetic-request", executor.submit(() -> {
                    assertNull(MDC.get("private-entry")); return MDC.get("requestId");
                }).get(5, TimeUnit.SECONDS));
                MDC.clear();
                assertNull(executor.submit(() -> MDC.get("requestId")).get(5, TimeUnit.SECONDS));
            }
        } finally { for (var executor : executors) { executor.shutdown(); } }
    }
    @Test void decoratorRestoresWorkerContextEvenWhenTaskFails() {
        var decorator = ApplicationTaskConfig.requestCorrelationDecorator();
        MDC.put("requestId", "caller");
        var failure = new IllegalStateException("synthetic");
        Runnable decorated = decorator.decorate(() -> {
            assertEquals("caller", MDC.get("requestId")); throw failure;
        });
        MDC.put("requestId", "worker");
        assertSame(failure, assertThrows(IllegalStateException.class, decorated::run));
        assertEquals("worker", MDC.get("requestId"));
        MDC.clear();
        Runnable withoutRequest = decorator.decorate(() -> assertNull(MDC.get("requestId")));
        MDC.put("requestId", "worker"); withoutRequest.run();
        assertEquals("worker", MDC.get("requestId"));
    }
    @Test void singleWorkerNeverCarriesAnEarlierRequestIntoAnUnrelatedTask() throws Exception {
        var executor = new ThreadPoolTaskExecutor(); executor.setCorePoolSize(1); executor.setMaxPoolSize(1);
        executor.setTaskDecorator(ApplicationTaskConfig.requestCorrelationDecorator()); executor.initialize();
        try {
            MDC.put("requestId", "first");
            assertEquals("first", executor.submit(() -> MDC.get("requestId")).get(5, TimeUnit.SECONDS));
            MDC.clear();
            assertNull(executor.submit(() -> MDC.get("requestId")).get(5, TimeUnit.SECONDS));
            MDC.put("requestId", "second");
            assertEquals("second", executor.submit(() -> MDC.get("requestId")).get(5, TimeUnit.SECONDS));
            MDC.clear();
            assertNull(executor.submit(() -> MDC.get("requestId")).get(5, TimeUnit.SECONDS));
        } finally { executor.shutdown(); }
    }
}
