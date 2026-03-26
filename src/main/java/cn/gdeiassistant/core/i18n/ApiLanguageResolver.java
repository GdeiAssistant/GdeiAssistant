package cn.gdeiassistant.core.i18n;

public final class ApiLanguageResolver {

    private ApiLanguageResolver() {
    }

    public static String normalizeLanguage(String header) {
        if (header == null || header.isBlank()) {
            return "zh-CN";
        }
        String lang = header.split(",")[0].trim().split(";")[0].trim();
        switch (lang) {
            case "zh-HK":
            case "zh-Hant-HK":
                return "zh-HK";
            case "zh-TW":
            case "zh-Hant-TW":
            case "zh-Hant":
                return "zh-TW";
            case "ja":
            case "ja-JP":
                return "ja";
            case "ko":
            case "ko-KR":
                return "ko";
            case "en":
            case "en-US":
            case "en-GB":
                return "en";
            default:
                if (lang.startsWith("zh")) {
                    return "zh-CN";
                }
                if (lang.startsWith("ja")) {
                    return "ja";
                }
                if (lang.startsWith("ko")) {
                    return "ko";
                }
                if (lang.startsWith("en")) {
                    return "en";
                }
                return "zh-CN";
        }
    }

    public static boolean isSimplifiedChinese(String language) {
        return "zh-CN".equals(normalizeLanguage(language));
    }
}
