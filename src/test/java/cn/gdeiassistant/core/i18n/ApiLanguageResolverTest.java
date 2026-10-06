package cn.gdeiassistant.core.i18n;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiLanguageResolverTest {

    @ParameterizedTest
    @CsvSource({
            "ZH_hAnT_hK,zh-HK", "zh_HK,zh-HK", "zh-MO,zh-HK", "zh-Hant-MO,zh-HK",
            "zh_Hant_TW,zh-TW", "ZH-TW,zh-TW", "zh-Hant,zh-TW", "zh-SG,zh-CN",
            "zh_Hant_HK_u_nu_hanidec,zh-HK", "zh-Hant-MO-u-ca-chinese,zh-HK",
            "EN_au,en", "JA_jp,ja", "KO_kr,ko", "fr-FR,zh-CN"
    })
    void normalizesSupportedRegionalAliases(String input, String expected) {
        assertEquals(expected, ApiLanguageResolver.normalizeLanguage(input));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            " zh_HK;q=0.9,en-US;q=0.8 |zh-HK",
            " ja-JP,en;q=0.5 |ja",
            " ko-KR;q=1 |ko"
    })
    void usesFirstLanguageWithoutQualityParameters(String header, String expected) {
        assertEquals(expected, ApiLanguageResolver.normalizeLanguage(header));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", ";q=0.9", ",en-US"})
    void fallsBackForMissingOrEmptyLanguage(String input) {
        assertEquals("zh-CN", ApiLanguageResolver.normalizeLanguage(input));
    }
}
