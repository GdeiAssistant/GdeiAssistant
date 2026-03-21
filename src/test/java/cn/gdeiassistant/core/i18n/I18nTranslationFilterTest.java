package cn.gdeiassistant.core.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class I18nTranslationFilterTest {

    @Mock
    private I18nTranslationService translationService;

    @Mock
    private FilterChain filterChain;

    private ObjectMapper objectMapper;
    private I18nTranslationFilter filter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new I18nTranslationFilter(translationService, objectMapper);
    }

    @Test
    void filter_passesThroughWhenAcceptLanguageIsZhCN() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "zh-CN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(translationService);
    }

    @Test
    void filter_passesThroughWhenAcceptLanguageIsAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(translationService);
    }

    @Test
    void filter_passesThroughForNonWhitelistedPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "ja-JP");
        request.setRequestURI("/api/user/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(translationService);
    }

    @Test
    void filter_replacesFieldValueWhenCacheHitExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "ja-JP");
        request.setRequestURI("/api/photograph/id/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCharacterEncoding("UTF-8");

        byte[] jsonBytes = "{\"success\":true,\"data\":{\"title\":\"校园风光\",\"content\":\"美丽的校园\"}}"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);

        doAnswer(invocation -> {
            org.springframework.web.util.ContentCachingResponseWrapper wrapper =
                    (org.springframework.web.util.ContentCachingResponseWrapper) invocation.getArgument(1);
            wrapper.setContentType("application/json;charset=UTF-8");
            wrapper.getOutputStream().write(jsonBytes);
            wrapper.flushBuffer();
            return null;
        }).when(filterChain).doFilter(eq(request), any());

        when(translationService.getCachedTranslation("校园风光", "ja")).thenReturn("キャンパスの風景");
        when(translationService.getCachedTranslation("美丽的校园", "ja")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        byte[] responseBytes = response.getContentAsByteArray();
        String responseBody = new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(responseBody.contains("キャンパスの風景"),
                "Expected translated text in response but got: " + responseBody);
        verify(translationService).enqueueTranslation("美丽的校园", "ja");
    }

    @Test
    void normalizeAcceptLanguage_mapsToExpectedLocales() throws Exception {
        Method method = I18nTranslationFilter.class.getDeclaredMethod("normalizeAcceptLanguage", String.class);
        method.setAccessible(true);

        assertEquals("ja", method.invoke(filter, "ja-JP"));
        assertEquals("zh-TW", method.invoke(filter, "zh-Hant"));
        assertEquals("en", method.invoke(filter, "fr"));
    }
}
