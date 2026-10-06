package cn.gdeiassistant.common.config.application;

import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.circuitbreaker.configuration.CommonCircuitBreakerConfigurationProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerConfigTest {

    @ParameterizedTest
    @ValueSource(strings = {"eduSystem", "cardSystem", "chsiClient", "libraryClient", "casClient"})
    void applicationConfigCreatesCircuitBreakersWithExpectedProtection(String name) throws Exception {
        StandardEnvironment environment = new StandardEnvironment();
        for (var source : new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"))) {
            environment.getPropertySources().addFirst(source);
        }
        var properties = Binder.get(environment).bind("resilience4j.circuitbreaker",
                Bindable.of(CommonCircuitBreakerConfigurationProperties.class)).get();
        var config = properties.createCircuitBreakerConfig(name, properties.getInstances().get(name),
                new CompositeCustomizer<CircuitBreakerConfigCustomizer>(List.of()));

        assertThat(properties.getInstances()).containsKey(name);
        assertThat(config.getSlidingWindowSize()).isEqualTo(10);
        assertThat(config.getFailureRateThreshold()).isEqualTo(50f);
        assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
        assertThat(config.getWaitIntervalFunctionInOpenState().apply(1)).isEqualTo(30_000L);
        assertThat(config.getSlowCallRateThreshold()).isEqualTo(80f);
        assertThat(config.getSlowCallDurationThreshold())
                .isEqualTo(Duration.ofSeconds("cardSystem".equals(name) ? 10 : 8));
    }
}
