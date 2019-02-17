package edu.gdei.gdeiassistant.Exception.QueryException;

/**
 * 快速连接教务系统时，若教务系统进行时间戳验证失败，则抛出该异常
 */
public class TimeStampIncorrectException extends Exception {

    public TimeStampIncorrectException() {
        super();
    }

    public TimeStampIncorrectException(String message) {
        super(message);
    }
}
