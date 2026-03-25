package cn.gdeiassistant.core.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class I18nTranslationFilterTest {

    @Test
    void unsupportedLanguageFallsBackToSimplifiedChinese() throws Exception {
        I18nTranslationFilter filter = new I18nTranslationFilter(null, new ObjectMapper());
        Method normalizeMethod = I18nTranslationFilter.class.getDeclaredMethod("normalizeAcceptLanguage", String.class);
        normalizeMethod.setAccessible(true);

        assertEquals("zh-CN", normalizeMethod.invoke(filter, "fr-FR,fr;q=0.9"));
        assertEquals("zh-CN", normalizeMethod.invoke(filter, "de-DE"));
    }

    @Test
    void supportedLanguagesKeepExpectedTargets() throws Exception {
        I18nTranslationFilter filter = new I18nTranslationFilter(null, new ObjectMapper());
        Method normalizeMethod = I18nTranslationFilter.class.getDeclaredMethod("normalizeAcceptLanguage", String.class);
        normalizeMethod.setAccessible(true);

        assertEquals("en", normalizeMethod.invoke(filter, "en-US"));
        assertEquals("ja", normalizeMethod.invoke(filter, "ja-JP"));
        assertEquals("ko", normalizeMethod.invoke(filter, "ko-KR"));
        assertEquals("zh-TW", normalizeMethod.invoke(filter, "zh-Hant"));
    }
}
