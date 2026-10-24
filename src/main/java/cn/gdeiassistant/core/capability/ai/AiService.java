package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import cn.gdeiassistant.common.exception.ProviderChainExhaustedException;
import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.core.capability.ProviderChain;
import cn.gdeiassistant.core.capability.ServiceProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

/** Reusable entry point for text, image and multi-turn business features; no public HTTP endpoint. */
@Service
public class AiService {
    private record Call(AiRequest request, Predicate<String> validResult) {}
    private record Route(List<String> names, ProviderChain<Call, String> chain) {}
    private final Map<String, Route> routes = new HashMap<>();
    private final AiProviderRegistry providers;
    private final AiModelCatalog catalog;
    private final MeterRegistry meters;
    private final CircuitBreakerRegistry breakers;
    private final Executor executor;
    private final Duration budget;

    public AiService(AiProviderRegistry providers, AiModelCatalog catalog, MeterRegistry meters,
                     CircuitBreakerRegistry breakers, @Qualifier("providerExecutor") Executor executor,
                     OutboundIntegrationProperties settings) {
        this.providers = providers; this.catalog = catalog; this.meters = meters; this.breakers = breakers;
        this.executor = executor; this.budget = Duration.ofMillis(settings.getProviderBudgetMs());
    }
    public String generate(String route, AiRequest request) throws ProviderChainExhaustedException {
        return generate(route, request, result -> result != null && !result.isBlank());
    }
    /** A business-specific validator makes unusable output fall through to the next model. */
    public String generate(String route, AiRequest request, Predicate<String> validResult) throws ProviderChainExhaustedException {
        return generate(route, request, validResult, List.of());
    }
    /** Business-owned local fallbacks share the same deadline and result validation. */
    public String generate(String route, AiRequest request, Predicate<String> validResult,
                           List<ServiceProvider<AiRequest, String>> fallbacks) throws ProviderChainExhaustedException {
        return route(route, fallbacks).execute(new Call(Objects.requireNonNull(request), Objects.requireNonNull(validResult)));
    }
    public List<Map<String, Object>> providerStatus(String route) { return providerStatus(route, List.of()); }
    public List<Map<String, Object>> providerStatus(String route, List<ServiceProvider<AiRequest, String>> fallbacks) {
        return route(route, fallbacks).getProviderStatus();
    }

    private synchronized ProviderChain<Call, String> route(String name, List<ServiceProvider<AiRequest, String>> fallbacks) {
        if (name == null || !name.matches("[a-z][a-z0-9-]{0,63}")) throw new IllegalArgumentException("Invalid AI route");
        var candidates = new ArrayList<>(providers.providers(name, catalog));
        candidates.addAll(fallbacks);
        var names = candidates.stream().map(ServiceProvider::providerName).toList();
        Route current = routes.get(name);
        if (current != null && current.names().equals(names)) return current.chain();
        List<ServiceProvider<Call, String>> adapters = candidates.stream().map(provider -> (ServiceProvider<Call, String>) new ServiceProvider<Call, String>() {
            public String providerName() { return provider.providerName(); }
            public int priority() { return provider.priority(); }
            public boolean isConfigured() { return provider.isConfigured(); }
            public boolean isHealthy() { return provider.isHealthy(); }
            public String execute(Call call) throws ProviderException { return provider.execute(call.request()); }
        }).toList();
        var chain = new ProviderChain<>("ai." + name, adapters, meters, breakers,
                (Call call, String result) -> call.validResult().test(result), executor, budget);
        routes.put(name, new Route(names, chain));
        return chain;
    }
}
