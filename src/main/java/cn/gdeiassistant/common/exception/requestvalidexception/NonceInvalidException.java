package cn.gdeiassistant.common.exception.requestvalidexception;

public class NonceInvalidException extends Exception {

    public NonceInvalidException() {
    }

    public NonceInvalidException(String message) {
        super(message);
    }
}
