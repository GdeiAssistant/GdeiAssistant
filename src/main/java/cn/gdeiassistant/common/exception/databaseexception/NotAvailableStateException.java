package cn.gdeiassistant.common.exception.databaseexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class NotAvailableStateException extends KeyedBusinessException {

    public NotAvailableStateException() {
    }

    public NotAvailableStateException(String message) {
        super(message);
    }

    private NotAvailableStateException(String key, Object[] args) {
        super(key, args, null);
    }

    public static NotAvailableStateException ofKey(String key, Object... args) {
        return new NotAvailableStateException(key, args);
    }
}
