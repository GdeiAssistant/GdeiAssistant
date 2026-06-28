package cn.gdeiassistant.core.capability;

import cn.gdeiassistant.common.exception.ProviderChainExhaustedException;
import cn.gdeiassistant.common.exception.ProviderException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用 Provider 链。
 * 按优先级维护一组 ServiceProvider，按序尝试，失败自动降级到下一个。
 * 每个 provider 有独立断路器，连续失败超过阈值后跳过。
 */
public class ProviderChain<T, R> {

    private static final Logger log = LoggerFactory.getLogger(ProviderChain.class);

    private final List<ServiceProvider<T, R>> providers;
    private final Map<String, CircuitBreaker> breakers = new HashMap<>();
    private final MeterRegistry meterRegistry;
    private final String chainName;

    /**
     * @param chainName       链名称，用于日志和 Metrics 标签
     * @param providers       所有 provider 实例（按优先级排序，未配置的被自动过滤）
     * @param meterRegistry   Micrometer MeterRegistry（可为 null 跳过 Metrics）
     * @param cbRegistry      Resilience4j CircuitBreakerRegistry（可为 null 使用默认配置）
     */
    public ProviderChain(String chainName, List<ServiceProvider<T, R>> providers,
                         MeterRegistry meterRegistry, CircuitBreakerRegistry cbRegistry) {
        this.chainName = chainName;
        this.meterRegistry = meterRegistry;

        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

        this.providers = providers.stream()
                .filter(ServiceProvider::isConfigured)
                .sorted(Comparator.comparingInt(ServiceProvider::priority))
                .collect(Collectors.toList());

        for (ServiceProvider<T, R> p : this.providers) {
            String name = p.providerName();
            CircuitBreaker cb = cbRegistry != null
                    ? cbRegistry.circuitBreaker(chainName + "." + name, defaultConfig)
                    : CircuitBreaker.ofDefaults(chainName + "." + name);
            breakers.put(name, cb);
        }

        log.info("[{}] ProviderChain 初始化完成：{} 个已配置 provider", chainName, this.providers.size());
    }

    /**
     * 按优先级依次尝试 provider：
     * 1. 跳过未配置的 provider
     * 2. 跳过已熔断的 provider
     * 3. 跳过不健康的 provider
     * 4. 成功 → 返回结果，记录 Metrics
     * 5. 失败 → 计入断路器，尝试下一个
     * 6. 全部失败 → 抛出 ProviderChainExhaustedException
     */
    public R execute(T request) throws ProviderChainExhaustedException {
        List<String> attempted = new ArrayList<>();

        for (ServiceProvider<T, R> provider : providers) {
            String name = provider.providerName();
            CircuitBreaker cb = breakers.get(name);

            if (cb != null && cb.getState() == CircuitBreaker.State.OPEN) {
                attempted.add(name + "(熔断)");
                log.warn("[{}] provider {} 已熔断，跳过", chainName, name);
                continue;
            }

            if (!provider.isHealthy()) {
                attempted.add(name + "(不健康)");
                log.warn("[{}] provider {} 健康检查未通过，跳过", chainName, name);
                continue;
            }

            long start = System.currentTimeMillis();
            try {
                R result;
                if (cb != null) {
                    result = cb.executeSupplier(() -> {
                        try {
                            return provider.execute(request);
                        } catch (ProviderException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    result = provider.execute(request);
                }
                long elapsed = System.currentTimeMillis() - start;
                recordMetric("success", name);
                log.info("[{}] provider {} 成功 ({}ms)", chainName, name, elapsed);
                return result;
            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - start;
                recordMetric("failure", name);
                log.warn("[{}] provider {} 失败 ({}ms): {}", chainName, name, elapsed,
                        e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                attempted.add(name);
            }
        }

        throw new ProviderChainExhaustedException(attempted);
    }

    /** 返回所有 provider 的状态信息（用于 HealthIndicator） */
    public List<Map<String, Object>> getProviderStatus() {
        List<Map<String, Object>> statuses = new ArrayList<>();
        for (ServiceProvider<T, R> p : providers) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("name", p.providerName());
            s.put("priority", p.priority());
            s.put("configured", p.isConfigured());
            s.put("healthy", p.isHealthy());
            CircuitBreaker cb = breakers.get(p.providerName());
            s.put("circuitBreaker", cb != null ? cb.getState().name() : "NONE");
            statuses.add(s);
        }
        return statuses;
    }

    /** 是否所有 provider 都不可用 */
    public boolean isCompletelyDown() {
        return providers.stream().noneMatch(p -> {
            CircuitBreaker cb = breakers.get(p.providerName());
            return p.isConfigured() && p.isHealthy()
                    && (cb == null || cb.getState() != CircuitBreaker.State.OPEN);
        });
    }

    public List<ServiceProvider<T, R>> getProviders() {
        return providers;
    }

    private void recordMetric(String outcome, String providerName) {
        if (meterRegistry != null) {
            meterRegistry.counter("provider." + outcome,
                    Tags.of("chain", chainName, "provider", providerName)).increment();
        }
    }
}
