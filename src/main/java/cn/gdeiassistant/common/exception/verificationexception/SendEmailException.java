package cn.gdeiassistant.common.exception.verificationexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 发送电子邮件验证码失败时，抛出该异常
 */
public class SendEmailException extends KeyedBusinessException {

    public SendEmailException() {
    }

    public SendEmailException(String message) {
        super(message);
    }

    private SendEmailException(String key, Object[] args) {
        super(key, args, null);
    }

    public static SendEmailException ofKey(String key, Object... args) {
        return new SendEmailException(key, args);
    }
}
