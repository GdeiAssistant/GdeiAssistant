package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Instant;
import java.util.*;
import static cn.gdeiassistant.core.capability.ai.AiRoutingTest.endpoint;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiCatalogStoreTest {
    @Test void cachedRetirementsSurviveRestartWithoutPersistingKeysAndAreScopedToEndpoint() {
        var redis=mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String,String> values=mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);var store=new AiCatalogStore(redis);
        var endpoint=endpoint("china",AiProperties.Region.MAINLAND,AiProtocol.OPENAI_CHAT);
        var snapshot=new AiModelCatalog.Snapshot(Set.of("vision-two"),Set.of("vision-one"),Instant.parse("2026-01-01T00:00:00Z"),true);
        store.save(endpoint,snapshot);
        var key=ArgumentCaptor.forClass(String.class);var body=ArgumentCaptor.forClass(String.class);
        verify(values).set(key.capture(),body.capture());assertFalse(body.getValue().contains("synthetic-only"));assertFalse(key.getValue().contains("synthetic-only"));
        when(values.get(key.getValue())).thenReturn(body.getValue());
        var restored=store.load(endpoint).orElseThrow();assertEquals(snapshot.models(),restored.models());
        assertEquals(snapshot.unavailableModels(),restored.unavailableModels());assertEquals(snapshot.updatedAt(),restored.updatedAt());assertFalse(restored.lastRefreshSucceeded());
        var settings=new AiProperties();settings.setEndpoints(List.of(endpoint));var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());
        var catalog=new AiModelCatalog(settings,registry,store);catalog.restore();assertTrue(catalog.isUnavailable("china","vision-one"));
        assertEquals(1,registry.providers("ocr",catalog).size());
        endpoint.setBaseUrl("https://other.invalid/v1");assertTrue(store.load(endpoint).isEmpty());
        when(values.get(anyString())).thenReturn("corrupt cache");assertTrue(store.load(endpoint).isEmpty());
        doThrow(new IllegalStateException("synthetic outage")).when(values).set(anyString(),anyString());assertDoesNotThrow(()->store.save(endpoint,snapshot));
    }
}
