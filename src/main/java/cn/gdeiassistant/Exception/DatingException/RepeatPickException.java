package cn.gdeiassistant.Exception.DatingException;

/**
 * 对方未拒绝前，不能发起多次撩一下请求，否则抛出该异常
 */
public class RepeatPickException extends Exception {

    public RepeatPickException() {
    }

    public RepeatPickException(String message) {
        super(message);
    }
}

