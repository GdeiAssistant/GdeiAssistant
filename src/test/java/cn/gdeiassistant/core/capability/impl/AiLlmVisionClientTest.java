package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiLlmVisionClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private AiLlmVisionClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new AiLlmVisionClient();
        ReflectionTestUtils.setField(client, "restTemplate", restTemplate);
    }

    @Test
    void shouldRejectUnsupportedProvider() {
        client.setProvider("unknown");

        RecognitionException exception = assertThrows(RecognitionException.class, () -> client.recognizeDigits("ZmFrZS1pbWFnZQ=="));

        assertEquals("AI识别服务配置不合法：api.ai.ocr.provider 仅支持 openai / gemini / claude / deepseek / doubao", exception.getMessage());
    }

    @Test
    void shouldRequireClaudeApiKeyWhenProviderIsClaude() {
        client.setProvider("claude");

        RecognitionException exception = assertThrows(RecognitionException.class, () -> client.recognizeDigits("ZmFrZS1pbWFnZQ=="));

        assertEquals("AI识别未启用：请配置 api.ai.ocr.claude.api-key", exception.getMessage());
    }

    @Test
    void shouldRecognizeDigitsWithClaudeMessagesApi() throws Exception {
        client.setProvider("claude");
        client.setClaudeApiKey("sk-ant-test");
        client.setClaudeModel("claude-sonnet-4-20250514");
        client.setClaudeApiUrl("https://api.anthropic.com/v1/messages");

        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-api-key", "sk-ant-test"))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"model\":\"claude-sonnet-4-20250514\"")))
                .andExpect(content().string(containsString("\"type\":\"image\"")))
                .andExpect(content().string(containsString("\"media_type\":\"image/png\"")))
                .andExpect(content().string(containsString("\"data\":\"ZmFrZS1pbWFnZQ==\"")))
                .andRespond(withSuccess("{\"content\":[{\"type\":\"text\",\"text\":\"123456\"}]}", MediaType.APPLICATION_JSON));

        assertEquals("123456", client.recognizeDigits("ZmFrZS1pbWFnZQ=="));
        server.verify();
    }

    @Test
    void shouldRecognizeDigitsWithDeepseekCompatibleApi() throws Exception {
        client.setProvider("deepseek");
        client.setDeepseekApiKey("sk-deepseek-test");
        client.setDeepseekModel("deepseek-chat");
        client.setDeepseekBaseUrl("https://api.deepseek.com/v1");

        server.expect(requestTo("https://api.deepseek.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer sk-deepseek-test"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("\"model\":\"deepseek-chat\"")))
                .andExpect(content().string(containsString("\"type\":\"image_url\"")))
                .andExpect(content().string(containsString("\"url\":\"data:image/png;base64,ZmFrZS1pbWFnZQ==\"")))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"7890\"}}]}", MediaType.APPLICATION_JSON));

        assertEquals("7890", client.recognizeDigits("ZmFrZS1pbWFnZQ=="));
        server.verify();
    }
}
