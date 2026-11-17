package cn.gdeiassistant.common.exception.databaseexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class ConfirmedStateException extends KeyedBusinessException {

    public ConfirmedStateException() {
    }

    public ConfirmedStateException(String message) {
        super(message);
    }

    private ConfirmedStateException(String key, Object[] args) {
        super(key, args, null);
    }

    public static ConfirmedStateException ofKey(String key, Object... args) {
        return new ConfirmedStateException(key, args);
    }
}
