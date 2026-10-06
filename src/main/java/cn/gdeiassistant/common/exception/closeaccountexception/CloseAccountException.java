package cn.gdeiassistant.common.exception.closeaccountexception;

public abstract class CloseAccountException extends Exception {

    public CloseAccountException() {
    }

    public CloseAccountException(String message) {
        super(message);
    }
}
