package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import cn.gdeiassistant.common.exception.ProviderChainExhaustedException;
import cn.gdeiassistant.core.capability.impl.LocalCaptchaOcrProvider;
import cn.gdeiassistant.core.capability.impl.OcrService;
import com.alibaba.fastjson2.JSONObject;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import java.util.*;
import static cn.gdeiassistant.core.capability.ai.AiRoutingTest.endpoint;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiServiceTest {
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    @org.junit.jupiter.api.AfterEach void closeMeters() { meters.close(); }
    @Test void reusableTextRouteFallsBackAndOcrConstraintsStayInBusinessLayer() throws Exception {
        var settings=new AiProperties();var endpoint=endpoint("china",AiProperties.Region.MAINLAND,AiProtocol.OPENAI_CHAT);
        endpoint.setRoutes(Map.of("chat",List.of("text-model"),"ocr",List.of("vision-one","vision-two")));
        settings.setEndpoints(List.of(endpoint));var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());
        var server=MockRestServiceServer.bindTo(registry.http()).build();
        for(String text:List.of("answer for a future chat feature","explanation instead of digits","1234")) {
            server.expect(requestTo("https://synthetic.invalid/v1/chat/completions"))
                    .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\""+text+"\"}}]}",MediaType.APPLICATION_JSON));
        }
        {
            var service=new AiService(registry,meters,CircuitBreakerRegistry.ofDefaults(),Runnable::run,new OutboundIntegrationProperties());
            assertEquals("answer for a future chat feature",service.generate("chat",AiRequest.text("question")));
            var local=mock(LocalCaptchaOcrProvider.class);when(local.isConfigured()).thenReturn(true);var ocr=new OcrService(service,local);
            assertEquals("1234",ocr.recognizeDigits("c3ludGhldGlj"));verify(local,never()).execute(any());
            assertEquals(3,ocr.getProviderStatus().size());
            assertThrows(ProviderChainExhaustedException.class,()->service.generate("summary",AiRequest.text("question")));
            assertThrows(IllegalArgumentException.class,()->service.generate("Invalid Route",AiRequest.text("question")));
            assertEquals("",ocr.recognizeDigits(""));
        }
        server.verify();
    }
    @Test void explicitlyConfiguredModelsAreTriedInOrderWithoutCatalogRequests() throws Exception {
        var settings=new AiProperties();var endpoint=endpoint("china",AiProperties.Region.MAINLAND,AiProtocol.OPENAI_CHAT);
        endpoint.setRoutes(Map.of("chat",List.of("model-v1","model-v2")));settings.setEndpoints(List.of(endpoint));
        var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());
        var server=MockRestServiceServer.bindTo(registry.http()).build();
        server.expect(requestTo("https://synthetic.invalid/v1/chat/completions"))
                .andExpect(request -> assertEquals("model-v1",JSONObject.parseObject(
                        ((org.springframework.mock.http.client.MockClientHttpRequest)request).getBodyAsString()).getString("model")))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo("https://synthetic.invalid/v1/chat/completions"))
                .andExpect(request -> assertEquals("model-v2",JSONObject.parseObject(
                        ((org.springframework.mock.http.client.MockClientHttpRequest)request).getBodyAsString()).getString("model")))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"configured fallback answer\"}}]}",MediaType.APPLICATION_JSON));
        var service=new AiService(registry,meters,CircuitBreakerRegistry.ofDefaults(),Runnable::run,new OutboundIntegrationProperties());
        assertEquals("configured fallback answer",service.generate("chat",AiRequest.text("question")));
        assertEquals(List.of("china/model-v1","china/model-v2"),service.providerStatus("chat").stream().map(status -> status.get("name")).toList());
        assertEquals(List.of("model-v1","model-v2"),endpoint.getRoutes().get("chat"));server.verify();
    }
    @Test void genericMessagesPreserveRolesImagesSystemInstructionsAndTokenBudgetAcrossProtocols() {
        var messages=List.of(AiRequest.Message.text(AiRequest.Role.SYSTEM,"system instruction"),
                AiRequest.Message.text(AiRequest.Role.USER,"previous question"),
                AiRequest.Message.text(AiRequest.Role.ASSISTANT,"previous answer"),
                new AiRequest.Message(AiRequest.Role.USER,"current question",List.of(new AiRequest.Image("image/jpeg","c3ludGhldGlj"))));
        var request=new AiRequest(messages,512);
        for(var protocol:AiProtocol.values()) {
            JSONObject body=protocol.request("selected-model",request);
            String serialized=body.toJSONString();assertTrue(serialized.contains("system instruction"));assertTrue(serialized.contains("previous answer"));
            assertTrue(serialized.contains("image/jpeg"));assertTrue(serialized.contains("512"));
            if(protocol==AiProtocol.ANTHROPIC) {assertEquals("system instruction",body.getString("system"));assertEquals(3,body.getJSONArray("messages").size());}
            if(protocol==AiProtocol.GEMINI) {assertNotNull(body.getJSONObject("systemInstruction"));assertEquals("model",body.getJSONArray("contents").getJSONObject(1).getString("role"));}
        }
        assertFalse(request.toString().contains("question"));assertFalse(messages.get(3).toString().contains("question"));assertFalse(messages.get(3).images().get(0).toString().contains("c3l"));
        assertThrows(UnsupportedOperationException.class,()->request.messages().clear());
        assertThrows(IllegalArgumentException.class,()->AiRequest.text(""));
        assertThrows(IllegalArgumentException.class,()->new AiRequest(messages,0));
        assertThrows(IllegalArgumentException.class,()->new AiRequest(List.of(),512));
        assertThrows(IllegalArgumentException.class,()->new AiRequest.Message(AiRequest.Role.SYSTEM,"x",List.of(new AiRequest.Image("image/png","x"))));
        assertThrows(IllegalArgumentException.class,()->new AiRequest.Image("text/plain","x"));
    }
}
