package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.core.capability.ServiceProvider;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/** Real serialization/HTTP contract tests; never call paid or external providers. */
class OcrHttpContractsTest {
    private ServiceProvider<OcrRequest,String> configure(String name,RestTemplate http) {
        ServiceProvider<OcrRequest,String> provider;
        switch(name){
            case "openai"->{var value=new OpenAiOcrProvider();assertFalse(value.isConfigured());value.setApiKey("synthetic-only");value.setModel("synthetic-model");provider=value;}
            case "claude"->{var value=new ClaudeOcrProvider();assertFalse(value.isConfigured());value.setApiKey("synthetic-only");value.setModel("synthetic-model");value.setApiUrl("https://synthetic.invalid/messages");provider=value;}
            case "deepseek"->{var value=new DeepSeekOcrProvider();assertFalse(value.isConfigured());value.setApiKey("synthetic-only");value.setModel("synthetic-model");value.setBaseUrl("https://synthetic.invalid/v1/");provider=value;}
            default->{var value=new DoubaoOcrProvider();assertFalse(value.isConfigured());value.setApiKey("synthetic-only");value.setModel("synthetic-model");value.setBaseUrl("https://synthetic.invalid/v1");provider=value;}
        }
        ReflectionTestUtils.setField(provider,"restTemplate",http);assertTrue(provider.isConfigured());assertEquals(name,provider.providerName());assertTrue(provider.priority()>=0);return provider;
    }
    private String url(String name){return name.equals("openai")?"https://api.openai.com/v1/responses":name.equals("claude")?"https://synthetic.invalid/messages":"https://synthetic.invalid/v1/chat/completions";}
    private void expectRequest(MockRestServiceServer server,String name,String response) {
        server.expect(requestTo(url(name))).andExpect(method(HttpMethod.POST))
                .andExpect(header(name.equals("claude")?"x-api-key":"Authorization",name.equals("claude")?"synthetic-only":"Bearer synthetic-only"))
                .andExpect(request->{
                    var body=JSONObject.parseObject(((org.springframework.mock.http.client.MockClientHttpRequest)request).getBodyAsString());
                    assertEquals("synthetic-model",body.getString("model"));
                    String serialized=body.toJSONString();assertTrue(serialized.contains("c3ludGhldGlj"));assertTrue(serialized.contains("Extract only digits"));assertFalse(serialized.contains("synthetic-only"));
                }).andRespond(withSuccess(response,MediaType.APPLICATION_JSON));
    }
    @Test void providersReadTheirSupportedTextResponseShapes() throws Exception {
        var fixtures=Map.of("openai",List.of("{\"output_text\":\"1234\"}","{\"output\":[{\"content\":[{\"text\":\"1234\"}]}]}"),
                "claude",List.of("{\"content\":[{\"type\":\"thinking\",\"text\":\"ignore\"},{\"type\":\"text\",\"text\":\"1234\"}]}"),
                "deepseek",List.of("{\"choices\":[{\"message\":{\"content\":\"1234\"}}]}","{\"choices\":[{\"message\":{\"content\":[{\"type\":\"image\"},{\"type\":\"text\",\"text\":\"1234\"}]}}]}"),
                "doubao",List.of("{\"choices\":[{\"message\":{\"content\":\"1234\"}}]}","{\"choices\":[{\"message\":{\"content\":[{\"type\":\"text\",\"text\":\"1234\"}]}}]}"));
        for(var entry:fixtures.entrySet())for(String response:entry.getValue()) {
            var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();var provider=configure(entry.getKey(),http);
            expectRequest(server,entry.getKey(),response);assertEquals("1234",provider.execute(OcrRequest.forDigits("c3ludGhldGlj")),entry.getKey());server.verify();
        }
    }
    @Test void emptyMalformedAndHttpFailureResponsesNeverBecomeSuccessfulText() throws Exception {
        for(String name:List.of("openai","claude","deepseek","doubao"))for(String response:List.of("","{}","invalid JSON")) {
            var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();var provider=configure(name,http);
            server.expect(requestTo(url(name))).andRespond(withSuccess(response,MediaType.APPLICATION_JSON));
            if(response.equals("invalid JSON"))assertThrows(ProviderException.class,()->provider.execute(OcrRequest.forDigits("c3ludGhldGlj")));
            else assertEquals("",provider.execute(OcrRequest.forDigits("c3ludGhldGlj")));server.verify();
        }
        for(String name:List.of("openai","claude","deepseek","doubao")) {
            var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();var provider=configure(name,http);
            server.expect(requestTo(url(name))).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
            assertThrows(ProviderException.class,()->provider.execute(OcrRequest.forDigits("c3ludGhldGlj")));server.verify();
        }
    }
}
