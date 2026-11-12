package cn.gdeiassistant.common.exception.commonexception;

public class PasswordIncorrectException extends Exception {

    public PasswordIncorrectException() {
        super();
    }

    public PasswordIncorrectException(String message) {
        super(message);
    }

    public PasswordIncorrectException(String message, Throwable cause) {
        super(message, cause);
    }
}
