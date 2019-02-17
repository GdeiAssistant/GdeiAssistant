package edu.gdei.gdeiassistant.Exception.ChargeException;

/**
 * 安全校验不通过时，抛出该异常
 */
public class SecurityInvalidException extends Exception {

    public SecurityInvalidException() {
    }

    public SecurityInvalidException(String message) {
        super(message);
    }
}
