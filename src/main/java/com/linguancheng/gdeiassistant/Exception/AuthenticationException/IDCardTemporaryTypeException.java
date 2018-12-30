package com.linguancheng.gdeiassistant.Exception.AuthenticationException;

/**
 * 身份证类型为临时身份证时，抛出该异常
 */
public class IDCardTemporaryTypeException extends Exception {

    public IDCardTemporaryTypeException() {
    }

    public IDCardTemporaryTypeException(String message) {
        super(message);
    }
}
