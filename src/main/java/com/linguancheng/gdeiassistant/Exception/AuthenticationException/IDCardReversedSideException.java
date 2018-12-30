package com.linguancheng.gdeiassistant.Exception.AuthenticationException;

/**
 * 身份证正反面颠倒时，抛出该异常
 */
public class IDCardReversedSideException extends Exception {

    public IDCardReversedSideException() {
    }

    public IDCardReversedSideException(String message) {
        super(message);
    }
}
