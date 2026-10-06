package cn.gdeiassistant.core.i18n;

import java.util.Locale;

public final class ApiLanguageResolver {

    private ApiLanguageResolver() {
    }

    public static String normalizeLanguage(String header) {
        if (header == null || header.isBlank()) {
            return "zh-CN";
        }
        String lang = header.split(",", 2)[0].split(";", 2)[0].trim()
                .replace('_', '-').toLowerCase(Locale.ROOT);
        if (lang.startsWith("zh-hant-hk") || lang.startsWith("zh-hant-mo")
                || lang.startsWith("zh-hk") || lang.startsWith("zh-mo")) {
            return "zh-HK";
        }
        if (lang.startsWith("zh-tw") || lang.startsWith("zh-hant")) {
            return "zh-TW";
        }
        if (lang.startsWith("ja")) return "ja";
        if (lang.startsWith("ko")) return "ko";
        if (lang.startsWith("en")) return "en";
        return "zh-CN";
    }

    public static boolean isSimplifiedChinese(String language) {
        return "zh-CN".equals(normalizeLanguage(language));
    }
}
