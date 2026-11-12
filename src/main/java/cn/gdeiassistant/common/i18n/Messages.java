package cn.gdeiassistant.common.i18n;

/**
 * Developer-facing entry point for user-visible messages.
 *
 * <p>Always pass a catalog key. The returned value is the Simplified Chinese rendering, because
 * exceptions and legacy response builders carry text, not keys; {@link MessageCatalog} resolves
 * that text back to the key when the response is localized for the caller's language.
 */
public final class Messages {

    private Messages() {
    }

    public static String get(String key, Object... args) {
        return MessageCatalog.text(key, args);
    }

    public static String forLanguage(String key, String language, Object... args) {
        return MessageCatalog.get(key, language, args);
    }
}
