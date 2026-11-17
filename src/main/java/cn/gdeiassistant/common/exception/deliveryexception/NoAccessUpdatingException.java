package cn.gdeiassistant.common.exception.deliveryexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 当前用户无权限修改该交易信息时抛出该异常
 */
public class NoAccessUpdatingException extends KeyedBusinessException {

    public NoAccessUpdatingException() {
    }

    public NoAccessUpdatingException(String message) {
        super(message);
    }

    private NoAccessUpdatingException(String key, Object[] args) {
        super(key, args, null);
    }

    public static NoAccessUpdatingException ofKey(String key, Object... args) {
        return new NoAccessUpdatingException(key, args);
    }
}
