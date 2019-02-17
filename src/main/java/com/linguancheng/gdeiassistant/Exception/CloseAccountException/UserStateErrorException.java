package com.linguancheng.gdeiassistant.Exception.CloseAccountException;

/**
 * 用户账号状态异常时抛出该异常
 */
public class UserStateErrorException extends CloseAccountException {

    public UserStateErrorException() {
    }

    public UserStateErrorException(String message) {
        super(message);
    }
}
