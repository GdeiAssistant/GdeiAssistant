package edu.gdei.gdeiassistant.Exception.ChargeException;

/**
 * 用户身份凭证过期时抛出该异常
 */
public class RequestExpiredException extends Exception {

    public RequestExpiredException() {

    }

    public RequestExpiredException(String message) {
        super(message);
    }
}
