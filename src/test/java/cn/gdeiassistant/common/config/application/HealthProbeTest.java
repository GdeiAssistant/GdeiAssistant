package cn.gdeiassistant.common.config.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.availability.ApplicationAvailabilityAutoConfiguration;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.AvailabilityProbesAutoConfiguration;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.boot.health.autoconfigure.application.AvailabilityHealthContributorAutoConfiguration;
import org.springframework.boot.health.autoconfigure.registry.HealthContributorRegistryAutoConfiguration;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class HealthProbeTest {

    @Test
    void livenessSkipsDependenciesWhileFullHealthStillChecksThem() {
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withConfiguration(AutoConfigurations.of(
                        ApplicationAvailabilityAutoConfiguration.class,
                        AvailabilityHealthContributorAutoConfiguration.class,
                        HealthContributorRegistryAutoConfiguration.class,
                        HealthEndpointAutoConfiguration.class,
                        AvailabilityProbesAutoConfiguration.class))
                .withUserConfiguration(DependencyConfiguration.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    AvailabilityChangeEvent.publish(context, LivenessState.CORRECT);
                    HealthEndpoint endpoint = context.getBean(HealthEndpoint.class);
                    AtomicInteger calls = context.getBean(AtomicInteger.class);

                    assertThat(endpoint.healthForPath("liveness").getStatus()).isEqualTo(Status.UP);
                    assertThat(calls.get()).isZero();
                    assertThat(endpoint.health().getStatus()).isEqualTo(Status.DOWN);
                    assertThat(calls.get()).isEqualTo(1);

                    AvailabilityChangeEvent.publish(context, LivenessState.BROKEN);
                    assertThat(endpoint.healthForPath("liveness").getStatus()).isEqualTo(Status.DOWN);
                    assertThat(calls.get()).isEqualTo(1);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class DependencyConfiguration {
        @Bean
        AtomicInteger dependencyCalls() {
            return new AtomicInteger();
        }

        @Bean
        HealthIndicator syntheticDependency(AtomicInteger dependencyCalls) {
            return () -> {
                dependencyCalls.incrementAndGet();
                return Health.down().build();
            };
        }
    }
}
