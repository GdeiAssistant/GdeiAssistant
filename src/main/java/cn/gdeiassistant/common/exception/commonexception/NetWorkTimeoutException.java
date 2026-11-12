package cn.gdeiassistant.common.exception.commonexception;

public class NetWorkTimeoutException extends Exception {

    public NetWorkTimeoutException() {
    }

    public NetWorkTimeoutException(String message) {
        super(message);
    }

    public NetWorkTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
