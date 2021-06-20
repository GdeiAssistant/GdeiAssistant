package cn.gdeiassistant.Exception.ExpressException;

/**
 * 当参与猜一下功能，表白者没有填写真实姓名时，抛出该异常
 */
public class NoRealNameException extends ExpressGuessException {

    public NoRealNameException() {

    }

    public NoRealNameException(String message) {
        super(message);
    }
}
