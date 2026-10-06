package cn.gdeiassistant.common.exception.tokenvalidexception;

public class TokenExpiredException extends Exception {

    public TokenExpiredException() {
        super();
    }

    public TokenExpiredException(String message) {
        super(message);
    }
}
