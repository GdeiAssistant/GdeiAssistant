package cn.gdeiassistant.common.exception.queryexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 快速连接教务系统时，若教务系统进行时间戳验证失败，则抛出该异常
 */
public class TimeStampIncorrectException extends KeyedBusinessException {

    public TimeStampIncorrectException() {
        super();
    }

    public TimeStampIncorrectException(String message) {
        super(message);
    }

    private TimeStampIncorrectException(String key, Object[] args) {
        super(key, args, null);
    }

    public static TimeStampIncorrectException ofKey(String key, Object... args) {
        return new TimeStampIncorrectException(key, args);
    }
}
