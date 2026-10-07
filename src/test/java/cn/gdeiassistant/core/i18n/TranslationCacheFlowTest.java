package cn.gdeiassistant.core.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationCacheFlowTest {
    private final I18nConfig config = new I18nConfig();
    private final RestTemplate http = mock(RestTemplate.class);
    private final RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final I18nTranslationService service = new I18nTranslationService(config);

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(config, "deeplApiKey", "synthetic-key");
        ReflectionTestUtils.setField(config, "deeplApiUrl", "https://translation.example.invalid");
        ReflectionTestUtils.setField(config, "cacheTtlDays", 7);
        ReflectionTestUtils.setField(service, "restTemplate", http);
        ReflectionTestUtils.setField(service, "redisTemplate", redis);
        when(redis.opsForValue()).thenReturn(values);
    }

    @Test void cachedTranslationUsesNormalizedHashedKeysAndNeverCallsTheProvider() {
        when(values.get(anyString())).thenReturn("translated");
        assertEquals("translated", service.translate(" e\u0301 ", "en"));
        assertEquals("translated", service.translate("é", "en"));
        var keys = ArgumentCaptor.forClass(String.class);
        verify(values, times(2)).get(keys.capture());
        assertEquals(keys.getAllValues().get(0), keys.getAllValues().get(1));
        assertTrue(keys.getValue().matches("i18n:en:[0-9a-f]{64}"));
        verifyNoInteractions(http);
    }

    @Test void successfulTranslationSendsLanguageAndHtmlModeThenCachesWithFiniteTtl() {
        when(http.exchange(eq("https://translation.example.invalid"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("translations", List.of(Map.of("text", "translated")))));
        assertEquals("translated", service.translate("<b>合成文本</b>", "en"));
        var request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http).exchange(anyString(), eq(HttpMethod.POST), request.capture(), eq(Map.class));
        assertEquals("DeepL-Auth-Key synthetic-key", request.getValue().getHeaders().getFirst("Authorization"));
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, request.getValue().getHeaders().getContentType());
        assertEquals(List.of("EN"), ((Map<?, ?>) request.getValue().getBody()).get("target_lang"));
        assertEquals(List.of("html"), ((Map<?, ?>) request.getValue().getBody()).get("tag_handling"));
        verify(values).set(matches("i18n:en:[0-9a-f]{64}"), eq("translated"), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test void unavailableCacheAndInterruptedProviderRetainTheOriginalResponse() {
        when(redis.opsForValue()).thenThrow(new IllegalStateException("synthetic cache outage"));
        when(http.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenAnswer(a -> { Thread.currentThread().interrupt(); throw new IllegalStateException("synthetic interruption"); });
        try {
            assertNull(service.translate("合成文本", "en"));
            assertTrue(Thread.currentThread().isInterrupted());
            verify(http, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
        } finally { Thread.interrupted(); }
    }

    @Test void queuedDuplicateUnicodeTextsUseOneRequestAndReleaseTheirSlotAfterCompletion() {
        var queue = new ArrayList<Runnable>();
        ReflectionTestUtils.setField(service, "i18nExecutor", (java.util.concurrent.Executor) queue::add);
        when(http.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("translations", List.of(Map.of("text", "后台")))));
        service.enqueueTranslation(" e\u0301 ", "zh-HK"); service.enqueueTranslation("é", "zh-HK");
        assertEquals(1, queue.size()); queue.remove(0).run();
        verify(values).set(anyString(), eq("後臺"), eq(7L), eq(TimeUnit.DAYS));
        service.enqueueTranslation("é", "zh-HK"); assertEquals(1, queue.size());
        assertNull(service.translate("text", "unsupported"));
        assertNull(service.translate(" ", "en")); assertNull(service.translate("text", null));
    }
}
