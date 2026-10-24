package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.core.capability.ai.*;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/** Synthetic HTTP contracts; no external/paid requests. */
class OcrHttpContractsTest {
    private AiModelProvider provider(AiProtocol protocol, RestTemplate http, boolean bearer) {
        var settings = new AiProperties();
        var endpoint = new AiProperties.Endpoint();
        endpoint.setId("synthetic");endpoint.setRegion(AiProperties.Region.MAINLAND);
        endpoint.setAccessMode(AiProperties.AccessMode.DIRECT);endpoint.setProtocol(protocol);
        endpoint.setBaseUrl("https://synthetic.invalid/v1/");endpoint.setApiKey("synthetic-only");
        if (bearer) endpoint.setAuthentication(AiProperties.Authentication.BEARER);
        return new AiModelProvider(settings,endpoint,"vision",http);
    }
    private String url(AiProtocol protocol) {
        return "https://synthetic.invalid/v1" + switch(protocol) {
            case OPENAI_CHAT -> "/chat/completions";case OPENAI_RESPONSES -> "/responses";
            case ANTHROPIC -> "/messages";case GEMINI -> "/models/vision:generateContent";
        };
    }
    @Test void protocolsSerializeImagesAndOnlyReadAnswerBlocks() throws Exception {
        Map<AiProtocol,List<String>> fixtures=Map.of(
            AiProtocol.OPENAI_RESPONSES,List.of("{\"output\":[{\"type\":\"reasoning\",\"content\":[{\"text\":\"ignore\"}]},{\"type\":\"message\",\"content\":[{\"type\":\"output_text\",\"text\":\"12\"},{\"type\":\"output_text\",\"text\":\"34\"}]}]}"),
            AiProtocol.ANTHROPIC,List.of("{\"content\":[{\"type\":\"thinking\",\"text\":\"ignore\"},{\"type\":\"text\",\"text\":\"1234\"}]}"),
            AiProtocol.OPENAI_CHAT,List.of("{\"choices\":[{\"message\":{\"content\":\"1234\"}}]}","{\"choices\":[{\"message\":{\"content\":[{\"type\":\"image\"},{\"type\":\"text\",\"text\":\"1234\"}]}}]}"),
            AiProtocol.GEMINI,List.of("{\"candidates\":[{\"content\":{\"parts\":[{\"thought\":true,\"text\":\"ignore\"},{\"text\":\"12\"},{\"text\":\"34\"}]}}]}"));
        for(var entry:fixtures.entrySet())for(String response:entry.getValue())for(boolean bearer:List.of(false,true)) {
            var protocol=entry.getKey();var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();
            var provider=provider(protocol,http,bearer);
            String header=bearer?"Authorization":switch(protocol){case ANTHROPIC->"x-api-key";case GEMINI->"x-goog-api-key";default->"Authorization";};
            server.expect(requestTo(url(protocol))).andExpect(method(HttpMethod.POST))
                .andExpect(header(header,header.equals("Authorization")?"Bearer synthetic-only":"synthetic-only"))
                .andExpect(request->{
                    String serialized=((org.springframework.mock.http.client.MockClientHttpRequest)request).getBodyAsString();
                    var body=JSONObject.parseObject(serialized);
                    if(protocol!=AiProtocol.GEMINI)assertEquals("vision",body.getString("model"));
                    if(protocol==AiProtocol.OPENAI_RESPONSES)assertFalse(body.getBoolean("store"));
                    assertTrue(serialized.contains("c3ludGhldGlj"));assertTrue(serialized.contains("Extract only digits"));assertFalse(serialized.contains("synthetic-only"));
                }).andRespond(withSuccess(response,MediaType.APPLICATION_JSON));
            assertEquals("1234",provider.execute(AiRequest.image("Extract only digits", "image/png", "c3ludGhldGlj")));
            assertEquals("synthetic/vision",provider.providerName());assertEquals(0,provider.priority());assertTrue(provider.isConfigured());server.verify();
        }
    }
    @Test void malformedEmptyRedirectAndHttpErrorsCannotBecomeSuccessfulText() throws Exception {
        for(var protocol:AiProtocol.values())for(String response:List.of("","{}","invalid JSON")) {
            var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();var provider=provider(protocol,http,false);
            server.expect(requestTo(url(protocol))).andRespond(withSuccess(response,MediaType.APPLICATION_JSON));
            if(response.equals("invalid JSON"))assertThrows(ProviderException.class,()->provider.execute(AiRequest.image("Extract only digits", "image/png", "synthetic")));
            else assertEquals("",provider.execute(AiRequest.image("Extract only digits", "image/png", "synthetic")));server.verify();
        }
        for(var protocol:AiProtocol.values()) {
            var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();var provider=provider(protocol,http,false);
            server.expect(requestTo(url(protocol))).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).body("private input or key"));
            var failure=assertThrows(ProviderException.class,()->provider.execute(AiRequest.image("Extract only digits", "image/png", "synthetic")));
            assertNull(failure.getCause());assertFalse(failure.getMessage().contains("private"));server.verify();
        }
        assertEquals("",AiProtocol.GEMINI.text("{\"candidates\":[]}"));
        assertEquals("",AiProtocol.OPENAI_CHAT.text("{\"choices\":[{}]}"));
        assertEquals("",AiProtocol.ANTHROPIC.text(null));
    }
    @Test void redirectsDoNotBecomeInferenceResults() {
        var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();
        var provider=provider(AiProtocol.OPENAI_CHAT,http,false);
        server.expect(requestTo(url(AiProtocol.OPENAI_CHAT))).andRespond(withStatus(HttpStatus.FOUND)
                .location(java.net.URI.create("https://other.invalid/messages")));
        assertThrows(ProviderException.class,()->provider.execute(AiRequest.text("synthetic prompt")));server.verify();
    }

}
