package cn.gdeiassistant.common.exception.commonexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class PasswordIncorrectException extends KeyedBusinessException {

    public PasswordIncorrectException() {
        super();
    }

    public PasswordIncorrectException(String message) {
        super(message);
    }

    public PasswordIncorrectException(String message, Throwable cause) {
        super(message, cause);
    }

    private PasswordIncorrectException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }

    public static PasswordIncorrectException ofKey(String key, Object... args) {
        return new PasswordIncorrectException(key, args, null);
    }
}
