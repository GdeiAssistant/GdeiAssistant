package cn.gdeiassistant.common.config.application;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ResilienceBootCompatibilityTest {

    @Test
    void configuredStarterStartsAndRegistersCircuitBreakersOnCurrentBootVersion() {
        ClassLoader classLoader = getClass().getClassLoader();
        Class<?>[] configurations = ImportCandidates.load(AutoConfiguration.class, classLoader)
                .getCandidates().stream()
                .filter(name -> name.startsWith("io.github.resilience4j.")
                        && (name.contains(".verifier.") || name.endsWith(".CircuitBreakerAutoConfiguration")))
                .map(name -> loadConfiguration(name, classLoader))
                .toArray(Class<?>[]::new);

        assertThat(configurations).hasSize(2);
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(configurations))
                .withPropertyValues("resilience4j.circuitbreaker.instances.startupProbe.slidingWindowSize=10")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CircuitBreakerRegistry.class);
                    assertThat(context.getBean(CircuitBreakerRegistry.class)
                            .circuitBreaker("startupProbe").getCircuitBreakerConfig().getSlidingWindowSize())
                            .isEqualTo(10);
                });
    }

    private static Class<?> loadConfiguration(String name, ClassLoader classLoader) {
        try {
            return Class.forName(name, false, classLoader);
        } catch (ClassNotFoundException exception) {
            throw new AssertionError("Missing starter configuration: " + name, exception);
        }
    }
}
