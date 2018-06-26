package com.linguancheng.gdeiassistant.Exception.ChargeException;

/**
 * 用户充值金额超过范围时抛出该异常
 */
public class AccountNotAvailableException extends Exception {

    public AccountNotAvailableException() {
        super();
    }

    public AccountNotAvailableException(String message) {
        super(message);
    }
}
