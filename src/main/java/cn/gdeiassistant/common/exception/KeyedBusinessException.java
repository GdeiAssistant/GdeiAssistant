package cn.gdeiassistant.common.exception;

import cn.gdeiassistant.common.i18n.KeyedMessage;
import cn.gdeiassistant.common.i18n.Messages;

/**
 * Base checked exception that optionally retains the catalog key and arguments for direct
 * target-language rendering while remaining compatible with legacy single-message constructors.
 */
public abstract class KeyedBusinessException extends Exception implements KeyedMessage {

    private final String messageKey;
    private final Object[] messageArgs;

    protected KeyedBusinessException() {
        super();
        this.messageKey = null;
        this.messageArgs = null;
    }

    protected KeyedBusinessException(String message) {
        super(message);
        this.messageKey = null;
        this.messageArgs = null;
    }

    protected KeyedBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.messageKey = null;
        this.messageArgs = null;
    }

    protected KeyedBusinessException(String messageKey, Object[] messageArgs, Throwable cause) {
        super(Messages.get(messageKey, messageArgs), cause);
        this.messageKey = messageKey;
        this.messageArgs = (messageArgs == null || messageArgs.length == 0) ? null : messageArgs.clone();
    }

    @Override
    public String messageKey() {
        return messageKey;
    }

    @Override
    public Object[] messageArgs() {
        return messageArgs == null ? new Object[0] : messageArgs.clone();
    }
}
