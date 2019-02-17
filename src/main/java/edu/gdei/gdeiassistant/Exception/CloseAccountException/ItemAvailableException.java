package edu.gdei.gdeiassistant.Exception.CloseAccountException;

/**
 * 关闭账号时，用户有未处理的社区功能信息时，抛出该异常
 */
public class ItemAvailableException extends CloseAccountException {

    public ItemAvailableException() {
    }

    public ItemAvailableException(String message) {
        super(message);
    }
}
