package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import cn.gdeiassistant.core.capability.ServiceProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.net.HttpURLConnection;
import java.util.List;

@Component
@org.springframework.boot.actuate.endpoint.annotation.Endpoint(id = "aimodels")
public class AiProviderRegistry {
    private final AiProperties settings;
    private final RestTemplate http;
    private final AiModelSelection selection;
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
        this.selection = new AiModelSelection(settings, http);
    }
    public RestTemplate http() { return http; }
    @org.springframework.scheduling.annotation.Scheduled(initialDelayString = "${api.ai.initial-delay-ms:30000}",
            fixedDelayString = "${api.ai.refresh-ms:21600000}")
    public void refreshModels() { selection.refresh(); }
    @org.springframework.boot.actuate.endpoint.annotation.ReadOperation
    public java.util.Map<String, AiModelSelection.Status> modelStatus() { return selection.status(); }
    public List<ServiceProvider<AiRequest, String>> providers(String route) { return providers(route, false); }
    public List<ServiceProvider<AiRequest, String>> providers(String route, boolean images) {
        return settings.getEndpoints().stream().filter(settings::permits).filter(AiProperties.Endpoint::isConfigured)
                .flatMap(endpoint -> selection.models(endpoint, route, images).stream()
                        .map(model -> (ServiceProvider<AiRequest, String>) new AiModelProvider(settings, endpoint, model, http))).toList();
    }
}
