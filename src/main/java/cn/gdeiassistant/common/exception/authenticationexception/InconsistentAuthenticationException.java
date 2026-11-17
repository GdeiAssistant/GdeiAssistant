package cn.gdeiassistant.common.exception.authenticationexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 中国居民身份证校验信息不一致时抛出该异常
 */
public class InconsistentAuthenticationException extends KeyedBusinessException {

    public InconsistentAuthenticationException() {
    }

    public InconsistentAuthenticationException(String message) {
        super(message);
    }

    private InconsistentAuthenticationException(String key, Object[] args) {
        super(key, args, null);
    }

    public static InconsistentAuthenticationException ofKey(String key, Object... args) {
        return new InconsistentAuthenticationException(key, args);
    }
}
