package edu.gdei.gdeiassistant.Exception.DeliveryException;

/**
 * 当前用户无权限修改该交易信息时抛出该异常
 */
public class NoAccessUpdatingException extends Exception {

    public NoAccessUpdatingException() {
    }

    public NoAccessUpdatingException(String message) {
        super(message);
    }
}
