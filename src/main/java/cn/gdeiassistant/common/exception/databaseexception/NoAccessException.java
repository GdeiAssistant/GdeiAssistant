package cn.gdeiassistant.common.exception.databaseexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class NoAccessException extends KeyedBusinessException {

    public NoAccessException() {
    }

    public NoAccessException(String message) {
        super(message);
    }

    private NoAccessException(String key, Object[] args) {
        super(key, args, null);
    }

    public static NoAccessException ofKey(String key, Object... args) {
        return new NoAccessException(key, args);
    }
}
