package cn.gdeiassistant.core.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationResponseTest {
    private final I18nTranslationService translations = mock(I18nTranslationService.class);
    private final ObjectMapper json = new ObjectMapper();
    private final I18nTranslationFilter filter = new I18nTranslationFilter(translations, json);
    private MockHttpServletResponse run(String path, String lang, String type, String body) throws Exception {
        var request = new MockHttpServletRequest("GET", path); if (lang != null) request.addHeader("Accept-Language", lang);
        var response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { res.setContentType(type); res.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8)); };
        filter.doFilter(request, response, chain); return response;
    }
    @Test void cachedTextReplacesOnlyAllowedFieldsAndArraysRetainStructure() throws Exception {
        when(translations.getCachedTranslation("标题", "en")).thenReturn("Title");
        var response = run("/api/information/news/1", "en", "application/json", """
          {"success":true,"data":{"title":"标题","content":"正文","username":"private-name","number":123}}
          """);
        var data = json.readTree(response.getContentAsByteArray()).get("data");
        assertEquals("Title", data.get("title").asText()); assertEquals("正文", data.get("content").asText());
        assertEquals("private-name", data.get("username").asText()); assertEquals(123, data.get("number").asInt());
        verify(translations).enqueueTranslation("正文", "en"); verify(translations, never()).enqueueTranslation(eq("private-name"), anyString());
        var list = run("/api/topic/list", "en", "application/json", "{\"data\":[{\"topic\":\"标题\",\"content\":\"\"},{\"topic\":123},null]}");
        assertEquals("Title", json.readTree(list.getContentAsByteArray()).get("data").get(0).get("topic").asText());
        assertEquals(list.getContentAsByteArray().length, list.getContentLength());
    }
    @Test void nestedTextArraysUseCacheAndEnqueueOnlyMissingNonblankText() throws Exception {
        when(translations.getCachedTranslation("节日", "ja")).thenReturn("祭り");
        when(translations.getCachedTranslation("第一段", "ja")).thenReturn("段落一");
        var response = run("/api/information/overview", "ja", "application/json", """
          {"data":{"festival":{"name":"节日","description":["第一段","第二段","",123]},"notices":[{"title":"通知"}]}}
          """);
        var festival = json.readTree(response.getContentAsByteArray()).path("data").path("festival");
        assertEquals("祭り", festival.get("name").asText()); assertEquals("段落一", festival.get("description").get(0).asText());
        assertEquals(123, festival.get("description").get(3).asInt());
        verify(translations).enqueueTranslation("第二段", "ja"); verify(translations, never()).enqueueTranslation(eq(""), anyString());
    }
    @Test void unsupportedPathsLanguagesBodiesAndFailuresPreserveOriginalResponse() throws Exception {
        for (String[] sample : new String[][]{
                {"/api/topic/1","zh-CN","application/json","{\"data\":{\"content\":\"正文\"}}"},
                {"/api/auth/login","en","application/json","{\"token\":\"synthetic\"}"},
                {"/api/topic/1","en","text/plain","unchanged"},
                {"/api/topic/1","en","application/json",""},
                {"/api/topic/1","en","application/json","malformed-json"}}) {
            assertArrayEquals(sample[3].getBytes(StandardCharsets.UTF_8), run(sample[0],sample[1],sample[2],sample[3]).getContentAsByteArray());
        }
        verifyNoInteractions(translations);
        when(translations.getCachedTranslation("正文","en")).thenThrow(new IllegalStateException("synthetic"));
        String original = "{\"data\":{\"content\":\"正文\"}}";
        assertArrayEquals(original.getBytes(StandardCharsets.UTF_8), run("/api/topic/1","en","application/json",original).getContentAsByteArray());
    }
}
