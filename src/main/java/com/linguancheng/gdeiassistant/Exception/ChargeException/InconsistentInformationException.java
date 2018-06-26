package com.linguancheng.gdeiassistant.Exception.ChargeException;

/**
 * 当校验用户姓名信息出现不一致的安全问题时，抛出该异常
 */
public class InconsistentInformationException extends Exception {

    public InconsistentInformationException() {

    }

    public InconsistentInformationException(String message) {
        super(message);
    }
}
