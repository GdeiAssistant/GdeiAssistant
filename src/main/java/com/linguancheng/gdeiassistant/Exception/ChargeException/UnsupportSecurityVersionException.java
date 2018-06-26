package com.linguancheng.gdeiassistant.Exception.ChargeException;

/**
 * 不支持当前安全版本时抛出该异常
 */
public class UnsupportSecurityVersionException extends Exception {

    public UnsupportSecurityVersionException() {
        super();
    }

    public UnsupportSecurityVersionException(String message) {
        super(message);
    }
}
