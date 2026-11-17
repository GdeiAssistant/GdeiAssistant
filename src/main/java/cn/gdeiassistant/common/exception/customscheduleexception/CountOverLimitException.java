package cn.gdeiassistant.common.exception.customscheduleexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class CountOverLimitException extends KeyedBusinessException {

    public CountOverLimitException() {
    }

    public CountOverLimitException(String message) {
        super(message);
    }

    private CountOverLimitException(String key, Object[] args) {
        super(key, args, null);
    }

    public static CountOverLimitException ofKey(String key, Object... args) {
        return new CountOverLimitException(key, args);
    }
}
