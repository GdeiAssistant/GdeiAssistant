package cn.gdeiassistant.common.exception.datingexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 对方未拒绝前，不能发起多次撩一下请求，否则抛出该异常
 */
public class RepeatPickException extends KeyedBusinessException {

    public RepeatPickException() {
    }

    public RepeatPickException(String message) {
        super(message);
    }

    private RepeatPickException(String key, Object[] args) {
        super(key, args, null);
    }

    public static RepeatPickException ofKey(String key, Object... args) {
        return new RepeatPickException(key, args);
    }
}

