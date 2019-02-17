package edu.gdei.gdeiassistant.Exception.AuthenticationException;

/**
 * 身份证照片类型为复印件时，抛出该异常
 */
public class IDCardCopyTypeException extends Exception {

    public IDCardCopyTypeException() {
    }

    public IDCardCopyTypeException(String message) {
        super(message);
    }
}
