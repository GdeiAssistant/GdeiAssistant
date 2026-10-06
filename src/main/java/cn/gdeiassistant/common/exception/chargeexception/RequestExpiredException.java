package cn.gdeiassistant.common.exception.chargeexception;

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
