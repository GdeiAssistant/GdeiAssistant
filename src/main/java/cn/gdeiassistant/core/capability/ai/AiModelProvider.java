package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.core.capability.ServiceProvider;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

/** One platform/model pair gets its own fallback state and circuit breaker. */
public class AiModelProvider implements ServiceProvider<AiRequest, String> {
    private final AiProperties settings;
    private final AiProperties.Endpoint endpoint;
    private final String model;
    private final RestTemplate http;

    public AiModelProvider(AiProperties settings, AiProperties.Endpoint endpoint, String model, RestTemplate http) {
        this.settings = settings;
        this.endpoint = endpoint;
        this.model = model;
        this.http = http;
    }
    @Override public String providerName() { return endpoint.getId() + "/" + model; }
    @Override public int priority() { return endpoint.getPriority(); }
    @Override public boolean isConfigured() { return endpoint.isConfigured() && settings.permits(endpoint); }
    @Override public String execute(AiRequest request) throws ProviderException {
        // Recheck at the final boundary, including calls outside ProviderChain.
        if (!isConfigured()) throw new ProviderException("AI endpoint unavailable under the configured region policy");
        try {
            AiProtocol protocol = endpoint.getProtocol();
            var response = http.exchange(protocol.inferenceUri(endpoint, model), org.springframework.http.HttpMethod.POST,
                    new HttpEntity<>(protocol.request(model, request), protocol.headers(endpoint)), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) throw new IllegalStateException("AI HTTP status");
            return protocol.text(response.getBody());
        } catch (Exception failure) {
            // HTTP exception messages/bodies can contain inputs or credentials; do not retain them.
            throw new ProviderException("AI request failed");
        }
    }
}
