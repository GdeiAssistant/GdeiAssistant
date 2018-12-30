package com.linguancheng.gdeiassistant.Exception.ChargeException;

/**
 * 用户充值金额超过范围时抛出该异常
 */
public class AmountNotAvailableException extends Exception {

    public AmountNotAvailableException() {
        super();
    }

    public AmountNotAvailableException(String message) {
        super(message);
    }
}
