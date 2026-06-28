package cn.gdeiassistant.common.exception;

import java.util.List;

/**
 * ProviderChain 中所有 provider 均不可用时抛出的异常。
 */
public class ProviderChainExhaustedException extends ProviderException {

    private final List<String> attemptedProviders;

    public ProviderChainExhaustedException(List<String> attemptedProviders) {
        super("所有服务提供者均不可用：" + String.join(" -> ", attemptedProviders));
        this.attemptedProviders = attemptedProviders;
    }

    public List<String> getAttemptedProviders() {
        return attemptedProviders;
    }
}
