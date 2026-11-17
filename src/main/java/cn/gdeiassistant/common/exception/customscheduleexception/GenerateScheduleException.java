package cn.gdeiassistant.common.exception.customscheduleexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class GenerateScheduleException extends KeyedBusinessException {

    public GenerateScheduleException() {
    }

    public GenerateScheduleException(String message) {
        super(message);
    }

    private GenerateScheduleException(String key, Object[] args) {
        super(key, args, null);
    }

    public static GenerateScheduleException ofKey(String key, Object... args) {
        return new GenerateScheduleException(key, args);
    }
}
