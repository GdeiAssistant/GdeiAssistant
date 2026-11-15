package cn.gdeiassistant.core.i18n;

import cn.gdeiassistant.core.capability.ai.AiProperties;
import cn.gdeiassistant.core.capability.ai.AiRequest;
import cn.gdeiassistant.core.capability.ai.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class I18nRegionComplianceTest {

    private static I18nConfig deeplConfig() {
        return new I18nConfig("test-deepl-key", "https://api-free.deepl.com/v2/translate", 7);
    }

    private static I18nConfig deeplConfigWithAiOptIn() {
        return new I18nConfig("test-deepl-key", "https://api-free.deepl.com/v2/translate", 7, true);
    }

    @Test
    void aiFallbackRequiresExplicitOptInInMainlandMode() {
        AiProperties aiProperties = new AiProperties();
        aiProperties.setRegion(AiProperties.Region.MAINLAND);
        aiProperties.setRegionRestrictionEnabled(true);

        AiService aiService = mock(AiService.class);
        RestTemplate restTemplate = new RestTemplate();

        I18nTranslationService service = new I18nTranslationService(
                deeplConfig(), null, Runnable::run, restTemplate, aiService, aiProperties);

        assertNull(service.translate("Hello campus", "zh-TW"));
        verifyNoInteractions(aiService);
    }

    @Test
    void mainlandRestrictedModeNeverCallsDeepLAndUsesAiTranslateRouteWhenOptedIn() throws Exception {
        AiProperties aiProperties = new AiProperties();
        aiProperties.setRegion(AiProperties.Region.MAINLAND);
        aiProperties.setRegionRestrictionEnabled(true);

        AiService aiService = mock(AiService.class);
        when(aiService.generate(eq("translate"), any(AiRequest.class))).thenReturn("简体中文提示");

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        @SuppressWarnings("unchecked")
        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);

        I18nTranslationService service = new I18nTranslationService(
                deeplConfigWithAiOptIn(), redis, Runnable::run, restTemplate, aiService, aiProperties);

        String result = service.translate("Hello campus", "zh-TW");
        assertEquals("簡體中文提示", result);
        verify(aiService).generate(eq("translate"), any(AiRequest.class));
        server.verify();
    }

    @Test
    void nonMainlandPermitsDeepLWhenConfigured() {
        AiProperties aiProperties = new AiProperties();
        aiProperties.setRegion(AiProperties.Region.OVERSEAS);
        aiProperties.setRegionRestrictionEnabled(true);

        AiService aiService = mock(AiService.class);
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("https://api-free.deepl.com/v2/translate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"translations\":[{\"text\":\"Hello\"}]}", MediaType.APPLICATION_JSON));

        I18nTranslationService service = new I18nTranslationService(
                deeplConfig(), null, Runnable::run, restTemplate, aiService, aiProperties);

        assertEquals("Hello", service.translate("你好", "en"));
        verifyNoInteractions(aiService);
        server.verify();
    }

    @Test
    void mainlandWithRestrictionDisabledPermitsDeepL() {
        AiProperties aiProperties = new AiProperties();
        aiProperties.setRegion(AiProperties.Region.MAINLAND);
        aiProperties.setRegionRestrictionEnabled(false);

        AiService aiService = mock(AiService.class);
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("https://api-free.deepl.com/v2/translate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"translations\":[{\"text\":\"Bonjour\"}]}", MediaType.APPLICATION_JSON));

        I18nTranslationService service = new I18nTranslationService(
                deeplConfig(), null, Runnable::run, restTemplate, aiService, aiProperties);

        assertNull(service.translate("你好", "fr"));
        assertEquals("Bonjour", service.translate("你好", "en"));
        verifyNoInteractions(aiService);
        server.verify();
    }
}
