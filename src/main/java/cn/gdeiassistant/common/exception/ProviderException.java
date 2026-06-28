package cn.gdeiassistant.common.exception;

/**
 * 服务提供者调用异常。
 * 由 ProviderChain 中的具体 provider 抛出，表示该 provider 执行失败。
 */
public class ProviderException extends Exception {

    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
