package cn.gdeiassistant.common.exception.TokenValidException;

public class TokenExpiredException extends Exception {

    public TokenExpiredException() {
        super();
    }

    public TokenExpiredException(String message) {
        super(message);
    }
}
