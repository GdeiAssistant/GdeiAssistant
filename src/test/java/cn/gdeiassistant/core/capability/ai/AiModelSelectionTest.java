package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import com.alibaba.fastjson2.JSONObject;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import java.util.*;
import static cn.gdeiassistant.core.capability.ai.AiRoutingTest.endpoint;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiModelSelectionTest {
    private static AiProperties.Selection family(boolean image) {
        var rule = new AiProperties.Selection();
        rule.setPattern("model-(?<version>[0-9]+(?:[.-][0-9]+)*)"); rule.setImageInput(image); return rule;
    }
    private static AiProperties settings(AiProperties.Endpoint endpoint) {
        endpoint.setRoutes(Map.of()); endpoint.setAutomaticRoutes(Map.of("chat", List.of(family(true))));
        var settings = new AiProperties(); settings.setEndpoints(List.of(endpoint)); settings.validate(); return settings;
    }
    private static List<String> names(AiProviderRegistry registry, boolean images) {
        return registry.providers("chat", images).stream().map(p -> p.providerName()).toList();
    }
    @Test void refreshActuallySwitchesSubsequentRequestsAndRetiresOldQueue() throws Exception {
        var endpoint = endpoint("china", AiProperties.Region.MAINLAND, AiProtocol.OPENAI_CHAT);
        var registry = new AiProviderRegistry(settings(endpoint), new OutboundIntegrationProperties());
        var server = MockRestServiceServer.bindTo(registry.http()).build();
        for (String version : List.of("9", "10")) {
            server.expect(requestTo("https://synthetic.invalid/v1/models")).andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("{\"data\":[{\"id\":\"model-"+version+"\"}]}", MediaType.APPLICATION_JSON));
            server.expect(requestTo("https://synthetic.invalid/v1/chat/completions"))
                    .andExpect(request -> assertEquals("model-"+version, JSONObject.parseObject(
                            ((org.springframework.mock.http.client.MockClientHttpRequest)request).getBodyAsString()).getString("model")))
                    .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"answer\"}}]}", MediaType.APPLICATION_JSON));
        }
        var meters = new SimpleMeterRegistry();
        try {
            var service = new AiService(registry, meters, CircuitBreakerRegistry.ofDefaults(), Runnable::run, new OutboundIntegrationProperties());
            assertTrue(names(registry, false).isEmpty());
            registry.refreshModels(); assertEquals("answer", service.generate("chat", AiRequest.text("question")));
            registry.refreshModels(); assertEquals("answer", service.generate("chat", AiRequest.text("question")));
            assertEquals(List.of("china/model-10"), names(registry, false));
        } finally { meters.close(); }
        server.verify();
    }
    @Test void numericFamilyVersionsIgnoreListTimestampsPreviewAndRetiredOrIncompatibleModels() {
        var endpoint = endpoint("china", AiProperties.Region.MAINLAND, AiProtocol.OPENAI_CHAT);
        var registry = new AiProviderRegistry(settings(endpoint), new OutboundIntegrationProperties());
        var server = MockRestServiceServer.bindTo(registry.http()).build();
        server.expect(anything()).andRespond(withSuccess("""
                {"data":[{"id":"model-9","created":99999999},
                  {"id":"model-10.1","created":1},
                  {"id":"model-10.2","capabilities":{"image_input":{"supported":false}}},
                  {"id":"model-11","lifecycle":"deprecated"},
                  {"id":"model-12","expiration_date":"2020-01-01"},
                  {"id":"model-13-preview"},{"id":"other-99"},
                  {"id":"model-14","architecture":{"output_modalities":["audio"]}}]}
                """, MediaType.APPLICATION_JSON));
        registry.refreshModels();
        assertEquals(List.of("china/model-10.2"), names(registry, false));
        assertEquals(List.of("china/model-10.1"), names(registry, true));
        assertEquals(List.of("model-10.1"), registry.modelStatus().get("china").imageSelected().get("chat")); server.verify();
    }
    @Test void snapshotDateNeverOutranksNextMinorReleaseAndNewerRevisionWinsSameRelease() {
        var endpoint = endpoint("china", AiProperties.Region.MAINLAND, AiProtocol.ANTHROPIC);
        var settings = settings(endpoint); var rule = family(true);
        rule.setPattern("claude-sonnet-(?<version>[0-9]+(?:[.-][0-9]{1,2})?)(?:-(?<revision>[0-9]{8}))?");
        endpoint.setAutomaticRoutes(Map.of("chat", List.of(rule))); settings.validate();
        var registry = new AiProviderRegistry(settings, new OutboundIntegrationProperties());
        var server = MockRestServiceServer.bindTo(registry.http()).build();
        server.expect(anything()).andRespond(withSuccess("""
                {"data":[{"id":"claude-sonnet-4-20250514"},{"id":"claude-sonnet-4-5-20250901"},
                {"id":"claude-sonnet-4-5-20250929"},{"id":"claude-sonnet-5","lifecycle":"preview"}]}
                """, MediaType.APPLICATION_JSON));
        registry.refreshModels(); assertEquals(List.of("china/claude-sonnet-4-5-20250929"), names(registry, false)); server.verify();
    }
    @Test void incompletePaginationAndHttpFailuresPreserveLastSuccessfulSelectionButEmptySuccessClearsIt() {
        var endpoint = endpoint("china", AiProperties.Region.MAINLAND, AiProtocol.ANTHROPIC);
        var registry = new AiProviderRegistry(settings(endpoint), new OutboundIntegrationProperties());
        var server = MockRestServiceServer.bindTo(registry.http()).build();
        server.expect(anything()).andRespond(withSuccess("{\"data\":[{\"id\":\"model-1\"}]}", MediaType.APPLICATION_JSON));
        server.expect(anything()).andRespond(withSuccess("{\"data\":[{\"id\":\"model-2\"}],\"has_more\":true,\"last_id\":\"model-2\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://synthetic.invalid/v1/models?after_id=model-2")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(anything()).andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));
        registry.refreshModels(); var timestamp = registry.modelStatus().get("china").updatedAt();
        registry.refreshModels(); assertEquals(List.of("china/model-1"), names(registry, false));
        assertFalse(registry.modelStatus().get("china").lastRefreshSucceeded());
        assertEquals(timestamp, registry.modelStatus().get("china").updatedAt());
        registry.refreshModels(); assertTrue(names(registry, false).isEmpty());
        assertTrue(registry.modelStatus().get("china").lastRefreshSucceeded()); server.verify();
    }
    @Test void googlePaginationAndSharedOpenCodeCatalogUseApprovedHostAndNativeAuthentication() {
        var endpoint = endpoint("china", AiProperties.Region.MAINLAND, AiProtocol.GEMINI);
        endpoint.setAuthentication(AiProperties.Authentication.BEARER); endpoint.setCatalogPath("/inference/v1/models");
        var registry = new AiProviderRegistry(settings(endpoint), new OutboundIntegrationProperties());
        var server = MockRestServiceServer.bindTo(registry.http()).build();
        server.expect(requestTo("https://synthetic.invalid/inference/v1/models"))
                .andExpect(header("Authorization", "Bearer synthetic-only"))
                .andRespond(withSuccess("{\"models\":[{\"name\":\"models/model-1\"}],\"nextPageToken\":\"next\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://synthetic.invalid/inference/v1/models?pageToken=next"))
                .andRespond(withSuccess("{\"models\":[{\"name\":\"models/model-2\",\"supportedGenerationMethods\":[\"generateContent\"]},{\"name\":\"models/model-3\",\"supportedGenerationMethods\":[\"embedContent\"]}]}", MediaType.APPLICATION_JSON));
        registry.refreshModels(); assertEquals(List.of("china/model-2"), names(registry, true)); server.verify();
    }
    @Test void aliasNeedsNoDiscoveryAndExplicitRouteOverridesAutomaticSelection() {
        var endpoint = endpoint("china", AiProperties.Region.MAINLAND, AiProtocol.OPENAI_CHAT);
        var settings = settings(endpoint); var alias = new AiProperties.Selection(); alias.setAlias("model-latest"); alias.setImageInput(true);
        endpoint.setAutomaticRoutes(Map.of("chat", List.of(alias))); settings.validate();
        var registry = new AiProviderRegistry(settings, new OutboundIntegrationProperties());
        var server = MockRestServiceServer.bindTo(registry.http()).build();
        registry.refreshModels(); assertEquals(List.of("china/model-latest"), names(registry, true));
        endpoint.setRoutes(Map.of("chat", List.of("model-pinned"))); registry.refreshModels();
        assertEquals(List.of("china/model-pinned"), names(registry, false));
        endpoint.setRoutes(Map.of()); settings.setAutoUpdate(false); registry.refreshModels(); assertTrue(names(registry, false).isEmpty()); server.verify();
    }
    @Test void regionAndMissingCredentialPreventEvenMetadataCallsAndTextFamiliesCannotReceiveImages() {
        var endpoint = endpoint("global", AiProperties.Region.OVERSEAS, AiProtocol.OPENAI_CHAT);
        var settings = settings(endpoint); var registry = new AiProviderRegistry(settings, new OutboundIntegrationProperties());
        var server = MockRestServiceServer.bindTo(registry.http()).build();
        registry.refreshModels(); assertTrue(registry.modelStatus().isEmpty());
        settings.setRegion(AiProperties.Region.OVERSEAS); endpoint.setApiKey(""); registry.refreshModels();
        endpoint.setApiKey("synthetic-only"); endpoint.setAutomaticRoutes(Map.of("chat", List.of(family(false))));
        server.expect(anything()).andRespond(withSuccess("{\"data\":[{\"id\":\"model-1\"}]}", MediaType.APPLICATION_JSON));
        registry.refreshModels(); assertEquals(List.of("global/model-1"), names(registry, false)); assertTrue(names(registry, true).isEmpty()); server.verify();
    }
    @Test void repeatedCursorsDoNotPublishPartialCatalogAndUnsafeConfigurationIsRejected() {
        var endpoint = endpoint("china", AiProperties.Region.MAINLAND, AiProtocol.GEMINI);
        var settings = settings(endpoint); var registry = new AiProviderRegistry(settings, new OutboundIntegrationProperties());
        var server = MockRestServiceServer.bindTo(registry.http()).build();
        for (int i=0; i<2; i++) server.expect(anything()).andRespond(withSuccess("{\"models\":[{\"name\":\"model-2\"}],\"nextPageToken\":\"repeated\"}", MediaType.APPLICATION_JSON));
        registry.refreshModels(); assertTrue(names(registry, false).isEmpty()); assertFalse(registry.modelStatus().get("china").lastRefreshSucceeded()); server.verify();
        for (String path : List.of("//evil.invalid/models", "https://evil.invalid/models", "/models?api_key=x", "/../models")) {
            endpoint.setCatalogPath(path); assertThrows(IllegalArgumentException.class, settings::validate);
        }
        endpoint.setCatalogPath("/models"); var rule = family(true); rule.setPattern("model-.*");
        endpoint.setAutomaticRoutes(Map.of("chat", List.of(rule))); assertThrows(IllegalArgumentException.class, settings::validate);
    }
}
