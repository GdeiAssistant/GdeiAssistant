package cn.gdeiassistant.common.exception.commonexception;

public class ServerErrorException extends Exception {

    public ServerErrorException() {
        super();
    }

    public ServerErrorException(String message) {
        super(message);
    }

    public ServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
