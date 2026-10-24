package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import cn.gdeiassistant.core.capability.ServiceProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.net.HttpURLConnection;
import java.util.List;

@Component
public class AiProviderRegistry {
    private final AiProperties settings;
    private final RestTemplate http;
    public AiProviderRegistry(AiProperties settings, OutboundIntegrationProperties timeouts) {
        this.settings = settings;
        // Never follow a redirect to another processing region or forward credentials to another host.
        var factory = new SimpleClientHttpRequestFactory() {
            @Override protected void prepareConnection(HttpURLConnection connection, String method) throws java.io.IOException {
                super.prepareConnection(connection, method);
                connection.setInstanceFollowRedirects(false);
            }
        };
        factory.setConnectTimeout(timeouts.getConnectTimeoutMs());
        factory.setReadTimeout(timeouts.getReadTimeoutMs());
        this.http = new RestTemplate(factory);
    }
    public RestTemplate http() { return http; }
    public List<ServiceProvider<AiRequest, String>> providers(String route) {
        return settings.getEndpoints().stream().filter(settings::permits).filter(AiProperties.Endpoint::isConfigured)
                .flatMap(endpoint -> endpoint.getRoutes().getOrDefault(route, List.of()).stream()
                        .map(model -> (ServiceProvider<AiRequest, String>) new AiModelProvider(settings, endpoint, model, http))).toList();
    }
}
