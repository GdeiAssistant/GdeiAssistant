package cn.gdeiassistant.common.exception.verificationexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 发送手机验证码失败时，抛出该异常
 */
public class SendSMSException extends KeyedBusinessException {

    public SendSMSException() {
    }

    public SendSMSException(String message) {
        super(message);
    }

    protected SendSMSException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }

    public static SendSMSException ofKey(String key, Object... args) {
        return new SendSMSException(key, args, null);
    }
}
