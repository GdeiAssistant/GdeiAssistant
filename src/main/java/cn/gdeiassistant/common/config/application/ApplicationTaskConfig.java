package cn.gdeiassistant.common.config.application;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@EnableAsync
public class ApplicationTaskConfig implements SchedulingConfigurer, AsyncConfigurer {

    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(10);
        executor.setTaskDecorator(requestCorrelationDecorator());
        executor.initialize();
        return executor;
    }

    @Bean("providerExecutor")
    public ThreadPoolTaskExecutor providerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("provider-");
        executor.setTaskDecorator(requestCorrelationDecorator());
        return executor;
    }

    @Bean("campusSyncExecutor")
    public Executor campusSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("campus-sync-");
        executor.setTaskDecorator(requestCorrelationDecorator());
        executor.initialize();
        return executor;
    }

    // Capture only the correlation ID, not arbitrary MDC entries from the request thread.
    static TaskDecorator requestCorrelationDecorator() {
        return task -> {
            String requestId = MDC.get("requestId");
            return () -> {
                String previous = MDC.get("requestId");
                try {
                    if (requestId == null) {
                        MDC.remove("requestId");
                    } else {
                        MDC.put("requestId", requestId);
                    }
                    task.run();
                } finally {
                    if (previous == null) {
                        MDC.remove("requestId");
                    } else {
                        MDC.put("requestId", previous);
                    }
                }
            };
        };
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor();
    }
}
