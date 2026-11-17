package cn.gdeiassistant.common.exception.commonexception;

import cn.gdeiassistant.common.i18n.KeyedMessage;
import cn.gdeiassistant.common.i18n.Messages;

/**
 * 功能未开启异常（特性开关 / 功能降级）。
 * 当第三方配置未配置或未启用时，业务层抛出此异常，由全局异常处理器统一返回 JSON。
 */
public class FeatureNotEnabledException extends RuntimeException implements KeyedMessage {

    private final String messageKey;
    private final Object[] messageArgs;

    public FeatureNotEnabledException() {
        super("该功能未启用");
        this.messageKey = null;
        this.messageArgs = null;
    }

    public FeatureNotEnabledException(String message) {
        super(message != null && !message.isEmpty() ? message : "该功能未启用");
        this.messageKey = null;
        this.messageArgs = null;
    }

    private FeatureNotEnabledException(String key, Object[] args) {
        super(Messages.get(key, args));
        this.messageKey = key;
        this.messageArgs = (args == null || args.length == 0) ? null : args.clone();
    }

    public static FeatureNotEnabledException ofKey(String key, Object... args) {
        return new FeatureNotEnabledException(key, args);
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
