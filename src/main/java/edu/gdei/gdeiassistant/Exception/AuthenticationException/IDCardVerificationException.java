package edu.gdei.gdeiassistant.Exception.AuthenticationException;

/**
 * 校验身份证不通过或错误时，抛出该异常
 */
public class IDCardVerificationException extends Exception {

    public IDCardVerificationException() {

    }

    public IDCardVerificationException(String message) {
        super(message);
    }
}
