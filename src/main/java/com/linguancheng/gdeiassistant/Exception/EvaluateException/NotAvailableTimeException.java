package com.linguancheng.gdeiassistant.Exception.EvaluateException;

/**
 * 非教学评教功能开放时间时抛出该异常
 */
public class NotAvailableTimeException extends Exception {

    public NotAvailableTimeException() {
        super();
    }

    public NotAvailableTimeException(String message) {
        super(message);
    }
}
