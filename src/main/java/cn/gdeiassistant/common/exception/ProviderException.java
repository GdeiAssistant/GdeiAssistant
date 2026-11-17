package cn.gdeiassistant.common.exception;

/**
 * 服务提供者调用异常。
 * 由 ProviderChain 中的具体 provider 抛出，表示该 provider 执行失败。
 */
public class ProviderException extends KeyedBusinessException {

    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ProviderException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }

    public static ProviderException ofKey(String key, Object... args) {
        return new ProviderException(key, args, null);
    }

    public static ProviderException ofKey(String key, Throwable cause, Object... args) {
        return new ProviderException(key, args, cause);
    }
}
