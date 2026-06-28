package cn.gdeiassistant.core.capability;

import cn.gdeiassistant.common.exception.ProviderException;

/**
 * 通用服务提供者接口。
 * 所有可替代的外部服务都实现此接口，通过 ProviderChain 统一管理优先级和容灾。
 *
 * @param <T> 请求参数类型
 * @param <R> 返回值类型
 */
public interface ServiceProvider<T, R> {

    /** 提供者唯一标识名，如 "deepseek", "tencent" */
    String providerName();

    /** 优先级：0 = 最高优先，数值越大优先级越低 */
    int priority();

    /**
     * 执行服务调用。
     * @param request 请求参数
     * @return 服务结果
     * @throws ProviderException 调用失败时抛出
     */
    R execute(T request) throws ProviderException;

    /** 该提供者是否已配置凭据/可用 */
    boolean isConfigured();

    /** 健康检查：默认使用 isConfigured()，子类可覆盖做真实探测 */
    default boolean isHealthy() {
        return isConfigured();
    }
}
