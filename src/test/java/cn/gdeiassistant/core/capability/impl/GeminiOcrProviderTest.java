package cn.gdeiassistant.core.capability.impl;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeminiOcrProviderTest {
    @Test void credentialsStayInHeaderAndThoughtPartsAreNotRecognizedAsAnswer() throws Exception {
        var http=new RestTemplate();var server=MockRestServiceServer.createServer(http);
        var provider=new GeminiOcrProvider();provider.setApiKey("synthetic-test-key");provider.setModel("gemini-3.8-flash");
        ReflectionTestUtils.setField(provider,"restTemplate",http);
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"))
                .andExpect(header("x-goog-api-key","synthetic-test-key"))
                .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[{\"thought\":true,\"text\":\"private reasoning\"},{\"text\":\"12\"},{\"text\":\"34\"}]}}]}",MediaType.APPLICATION_JSON));
        assertEquals("1234",provider.execute(OcrRequest.forDigits("synthetic-image")));server.verify();
    }
    @Test void missingCandidatesReturnInvalidOutputForChainFallback() throws Exception {
        var http=new RestTemplate();var server=MockRestServiceServer.createServer(http);
        var provider=new GeminiOcrProvider();provider.setApiKey("synthetic-test-key");provider.setModel("gemini-3.8-flash");
        ReflectionTestUtils.setField(provider,"restTemplate",http);
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"))
                .andRespond(withSuccess("{\"candidates\":[]}",MediaType.APPLICATION_JSON));
        assertEquals("",provider.execute(OcrRequest.forDigits("synthetic-image")));server.verify();
    }
}
