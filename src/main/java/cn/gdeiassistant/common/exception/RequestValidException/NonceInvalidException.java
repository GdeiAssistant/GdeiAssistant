package cn.gdeiassistant.common.exception.RequestValidException;

public class NonceInvalidException extends Exception {

    public NonceInvalidException() {
    }

    public NonceInvalidException(String message) {
        super(message);
    }
}
