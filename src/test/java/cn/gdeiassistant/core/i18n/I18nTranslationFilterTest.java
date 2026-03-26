package cn.gdeiassistant.core.i18n;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class I18nTranslationFilterTest {

    @Test
    void unsupportedLanguageFallsBackToSimplifiedChinese() {
        assertEquals("zh-CN", ApiLanguageResolver.normalizeLanguage("fr-FR,fr;q=0.9"));
        assertEquals("zh-CN", ApiLanguageResolver.normalizeLanguage("de-DE"));
    }

    @Test
    void supportedLanguagesKeepExpectedTargets() {
        assertEquals("en", ApiLanguageResolver.normalizeLanguage("en-US"));
        assertEquals("ja", ApiLanguageResolver.normalizeLanguage("ja-JP"));
        assertEquals("ko", ApiLanguageResolver.normalizeLanguage("ko-KR"));
        assertEquals("zh-TW", ApiLanguageResolver.normalizeLanguage("zh-Hant"));
    }
}
