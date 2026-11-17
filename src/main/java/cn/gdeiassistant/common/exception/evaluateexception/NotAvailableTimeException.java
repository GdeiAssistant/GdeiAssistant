package cn.gdeiassistant.common.exception.evaluateexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 非教学评教功能开放时间时抛出该异常
 */
public class NotAvailableTimeException extends KeyedBusinessException {

    public NotAvailableTimeException() {
        super();
    }

    public NotAvailableTimeException(String message) {
        super(message);
    }

    private NotAvailableTimeException(String key, Object[] args) {
        super(key, args, null);
    }

    public static NotAvailableTimeException ofKey(String key, Object... args) {
        return new NotAvailableTimeException(key, args);
    }
}
