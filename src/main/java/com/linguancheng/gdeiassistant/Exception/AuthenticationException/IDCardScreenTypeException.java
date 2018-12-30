package com.linguancheng.gdeiassistant.Exception.AuthenticationException;

/**
 * 身份证类型为翻拍时，抛出该异常
 */
public class IDCardScreenTypeException extends Exception {

    public IDCardScreenTypeException() {
    }

    public IDCardScreenTypeException(String message) {
        super(message);
    }
}
