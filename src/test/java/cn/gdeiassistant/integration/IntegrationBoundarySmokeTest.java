package cn.gdeiassistant.integration;

import cn.gdeiassistant.integration.card.CardClient;
import cn.gdeiassistant.integration.cas.CasClient;
import cn.gdeiassistant.integration.chsi.ChsiClient;
import cn.gdeiassistant.integration.edu.EduSystemClient;
import cn.gdeiassistant.integration.library.LibraryClient;
import cn.gdeiassistant.integration.news.NewsClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IntegrationBoundarySmokeTest {

    @Test
    void externalClientsKeepExplicitTimeoutBudgets() {
        assertEquals(15, readStaticInt(CardClient.class, "CARD_TIMEOUT_SEC"));
        assertEquals(10000, readStaticInt(CasClient.class, "TIMEOUT_MS"));
        assertEquals(15, readStaticInt(ChsiClient.class, "CHSI_TIMEOUT_SEC"));
        assertEquals(5, readStaticInt(ChsiClient.class, "OKHTTP_TIMEOUT_SEC"));
        assertEquals(15, readStaticInt(EduSystemClient.class, "EDU_REQUEST_TIMEOUT_SEC"));
        assertEquals(15, readStaticInt(LibraryClient.class, "LIBRARY_TIMEOUT_SEC"));
        assertEquals(10, readStaticInt(LibraryClient.class, "OKHTTP_TIMEOUT_SEC"));
        assertEquals(10000, readStaticInt(NewsClient.class, "TIMEOUT_MS"));
    }

    @Test
    void circuitBreakerProtectedMethodsKeepMatchingFallbacks() {
        assertCircuitBreakerFallbacks(CardClient.class);
        assertCircuitBreakerFallbacks(EduSystemClient.class);
    }

    private static void assertCircuitBreakerFallbacks(Class<?> type) {
        List<Method> protectedMethods = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(CircuitBreaker.class))
                .toList();

        assertFalse(protectedMethods.isEmpty(), () -> type.getSimpleName() + " 缺少 @CircuitBreaker 保护方法");

        for (Method method : protectedMethods) {
            CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
            Method fallback = findFallback(type, circuitBreaker.fallbackMethod(), method.getParameterTypes());
            assertNotNull(fallback, () -> type.getSimpleName() + "." + method.getName()
                    + " 缺少匹配的 fallback: " + circuitBreaker.fallbackMethod());
            assertEquals(method.getReturnType(), fallback.getReturnType(), () -> type.getSimpleName()
                    + "." + circuitBreaker.fallbackMethod() + " 返回值与原方法不一致");
        }
    }

    private static Method findFallback(Class<?> type, String fallbackName, Class<?>[] originalParams) {
        Class<?>[] fallbackParams = Arrays.copyOf(originalParams, originalParams.length + 1);
        fallbackParams[originalParams.length] = Throwable.class;
        try {
            return type.getDeclaredMethod(fallbackName, fallbackParams);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static int readStaticInt(Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("无法读取 " + type.getSimpleName() + "." + fieldName, e);
        }
    }
}
