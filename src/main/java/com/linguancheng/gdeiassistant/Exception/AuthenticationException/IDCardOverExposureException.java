package com.linguancheng.gdeiassistant.Exception.AuthenticationException;

/**
 * 身份证关键字段反光或过曝时，抛出该异常
 */
public class IDCardOverExposureException extends Exception {

    public IDCardOverExposureException() {
    }

    public IDCardOverExposureException(String message) {
        super(message);
    }
}
