package com.linguancheng.gdeiassistant.Exception.ChargeException;

/**
 * 不支持该客户端类型时抛出该异常
 */
public class UnsupportClientTypeException extends Exception {

    public UnsupportClientTypeException() {
        super();
    }

    public UnsupportClientTypeException(String message) {
        super(message);
    }
}
