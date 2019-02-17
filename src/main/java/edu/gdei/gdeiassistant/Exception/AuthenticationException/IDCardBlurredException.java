package edu.gdei.gdeiassistant.Exception.AuthenticationException;

/**
 * 身份证照片模糊时，抛出该异常
 */
public class IDCardBlurredException extends Exception {

    public IDCardBlurredException() {
    }

    public IDCardBlurredException(String message) {
        super(message);
    }
}
