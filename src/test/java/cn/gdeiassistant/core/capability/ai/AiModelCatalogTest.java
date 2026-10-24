package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import java.util.*;
import static cn.gdeiassistant.core.capability.ai.AiRoutingTest.endpoint;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiModelCatalogTest {
    @Test void discoveryIsRegionalAndNeverActivatesNewModelsAndKeepsLastGoodOnFailure() {
        var settings=new AiProperties();var china=endpoint("china",AiProperties.Region.MAINLAND,AiProtocol.OPENAI_CHAT);
        var overseas=endpoint("global",AiProperties.Region.OVERSEAS,AiProtocol.OPENAI_CHAT);
        settings.setEndpoints(List.of(china,overseas));var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());
        var server=MockRestServiceServer.bindTo(registry.http()).build();
        server.expect(requestTo("https://synthetic.invalid/v1/models")).andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization","Bearer synthetic-only"))
                .andRespond(withSuccess("{\"data\":[{\"id\":\"new-model\"}]}",MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://synthetic.invalid/v1/models")).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        var catalog=new AiModelCatalog(settings,registry);catalog.refresh();
        var good=catalog.snapshots().get("china");assertEquals(Set.of("new-model"),good.models());assertTrue(good.lastRefreshSucceeded());
        assertFalse(catalog.snapshots().containsKey("global"));assertEquals(List.of("vision-one","vision-two"),china.getRoutes().get("ocr"));
        assertEquals(2,registry.providers("ocr",new AiModelCatalog(settings,registry)).size());
        catalog.refresh();var stale=catalog.snapshots().get("china");assertEquals(good.models(),stale.models());
        assertEquals(good.updatedAt(),stale.updatedAt());assertFalse(stale.lastRefreshSucceeded());server.verify();
        assertThrows(UnsupportedOperationException.class,()->catalog.snapshots().clear());
        assertThrows(UnsupportedOperationException.class,()->stale.models().clear());
    }
    @Test void nativePaginationUsesOnlyCursorValuesAndPublishesCompleteSnapshot() {
        for(var protocol:List.of(AiProtocol.ANTHROPIC,AiProtocol.GEMINI)) {
            var settings=new AiProperties();var endpoint=endpoint("a",AiProperties.Region.MAINLAND,protocol);
            settings.setEndpoints(List.of(endpoint));var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());
            var server=MockRestServiceServer.bindTo(registry.http()).build();boolean google=protocol==AiProtocol.GEMINI;
            server.expect(requestTo("https://synthetic.invalid/v1/models")).andRespond(withSuccess(google
                    ?"{\"models\":[{\"name\":\"models/vision-new\"}],\"nextPageToken\":\"next/value\"}"
                    :"{\"data\":[{\"id\":\"vision-new\"}],\"has_more\":true,\"last_id\":\"next/value\"}",MediaType.APPLICATION_JSON));
            server.expect(requestTo("https://synthetic.invalid/v1/models?"+(google?"pageToken":"after_id")+"=next%2Fvalue"))
                    .andRespond(withSuccess(google?"{\"models\":[{\"name\":\"models/vision-two\"}]}":"{\"data\":[{\"id\":\"vision-two\"}],\"has_more\":false}",MediaType.APPLICATION_JSON));
            var catalog=new AiModelCatalog(settings,registry);catalog.refresh();
            assertEquals(Set.of("vision-new","vision-two"),catalog.snapshots().get("a").models());server.verify();
        }
    }
    @Test void opencodeGeminiUsesBearerAndSharedOpenAiShapedDirectory() {
        var settings=new AiProperties();var endpoint=endpoint("a",AiProperties.Region.OVERSEAS,AiProtocol.GEMINI);
        endpoint.setAuthentication(AiProperties.Authentication.BEARER);endpoint.setCatalogPath("/inference/v1/models");
        settings.setRegion(AiProperties.Region.OVERSEAS);settings.setEndpoints(List.of(endpoint));
        var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());var server=MockRestServiceServer.bindTo(registry.http()).build();
        server.expect(requestTo("https://synthetic.invalid/inference/v1/models")).andExpect(header("Authorization","Bearer synthetic-only"))
                .andRespond(withSuccess("{\"data\":[{\"id\":\"gemini-vision\"}]}",MediaType.APPLICATION_JSON));
        var catalog=new AiModelCatalog(settings,registry);catalog.refresh();assertEquals(Set.of("gemini-vision"),catalog.snapshots().get("a").models());server.verify();
    }
    @Test void disabledAndUnconfiguredEndpointsDoNotMakeRequests() {
        var settings=new AiProperties();var endpoint=endpoint("a",AiProperties.Region.MAINLAND,AiProtocol.OPENAI_CHAT);
        settings.setEndpoints(List.of(endpoint));settings.setCatalogEnabled(false);
        var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());var server=MockRestServiceServer.bindTo(registry.http()).build();
        var catalog=new AiModelCatalog(settings,registry);catalog.refresh();assertTrue(catalog.snapshots().isEmpty());
        settings.setCatalogEnabled(true);endpoint.setApiKey("");catalog.refresh();assertTrue(catalog.snapshots().isEmpty());server.verify();
    }
    @Test void malformedAndPartialCatalogsAreRejectedWithoutInventingAvailability() {
        for(String response:List.of("","null","{}","invalid JSON","{\"data\":[{}]}","{\"data\":[],\"has_more\":true}")) {
            var settings=new AiProperties();settings.setEndpoints(List.of(endpoint("a",AiProperties.Region.MAINLAND,AiProtocol.OPENAI_CHAT)));
            var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());var server=MockRestServiceServer.bindTo(registry.http()).build();
            server.expect(requestTo("https://synthetic.invalid/v1/models")).andRespond(withSuccess(response,MediaType.APPLICATION_JSON));
            var catalog=new AiModelCatalog(settings,registry);catalog.refresh();
            assertFalse(catalog.snapshots().get("a").lastRefreshSucceeded());assertNull(catalog.snapshots().get("a").updatedAt());server.verify();
        }
    }
    @Test void repeatedCursorDiscardsPartialRefresh() {
        var settings=new AiProperties();settings.setEndpoints(List.of(endpoint("a",AiProperties.Region.MAINLAND,AiProtocol.ANTHROPIC)));
        var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());var server=MockRestServiceServer.bindTo(registry.http()).build();
        String page="{\"data\":[{\"id\":\"vision\"}],\"has_more\":true,\"last_id\":\"same\"}";
        server.expect(requestTo("https://synthetic.invalid/v1/models")).andRespond(withSuccess(page,MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://synthetic.invalid/v1/models?after_id=same")).andRespond(withSuccess(page,MediaType.APPLICATION_JSON));
        var catalog=new AiModelCatalog(settings,registry);catalog.refresh();assertTrue(catalog.snapshots().get("a").models().isEmpty());server.verify();
    }
    @Test void vendorRetirementFlagsAndDatesExcludeModelsWithoutChargingForEvaluation() {
        var settings=new AiProperties();settings.setEndpoints(List.of(endpoint("a",AiProperties.Region.MAINLAND,AiProtocol.OPENAI_CHAT)));
        var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());var server=MockRestServiceServer.bindTo(registry.http()).build();
        server.expect(requestTo("https://synthetic.invalid/v1/models")).andRespond(withSuccess("{\"data\":["
                +"{\"id\":\"old\",\"status\":\"retired\"},{\"id\":\"old-two\",\"deprecated\":true},"
                +"{\"id\":\"expired\",\"expiration_date\":\"2020-01-01\"},"
                +"{\"id\":\"active\",\"expiration_date\":\"2099-01-01T00:00:00Z\"},"
                +"{\"id\":\"unknown\",\"expiration_date\":\"unknown format\"}]}",MediaType.APPLICATION_JSON));
        var catalog=new AiModelCatalog(settings,registry);catalog.refresh();
        assertEquals(Set.of("old","old-two","expired"),catalog.snapshots().get("a").unavailableModels());
        assertFalse(catalog.isUnavailable("a","active"));assertFalse(catalog.isUnavailable("a","unknown"));
        assertFalse(catalog.isUnavailable("unconfigured","old"));server.verify();
    }

}
