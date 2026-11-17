package cn.gdeiassistant.common.exception.expressexception;

/**
 * 当参与猜一下功能，表白者没有填写真实姓名时，抛出该异常
 */
public class NoRealNameException extends ExpressGuessException {

    public NoRealNameException() {

    }

    public NoRealNameException(String message) {
        super(message);
    }

    private NoRealNameException(String key, Object[] args) {
        super(key, args, null);
    }

    public static NoRealNameException ofKey(String key, Object... args) {
        return new NoRealNameException(key, args);
    }
}
