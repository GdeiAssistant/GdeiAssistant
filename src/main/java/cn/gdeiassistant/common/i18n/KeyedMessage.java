package cn.gdeiassistant.common.i18n;

/**
 * Carries an i18n catalog key and optional formatting arguments alongside a rendered message
 * so handlers can render the target language directly without reverse-matching Chinese text.
 */
public interface KeyedMessage {

    String messageKey();

    default Object[] messageArgs() {
        return new Object[0];
    }
}
