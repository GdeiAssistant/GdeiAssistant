package cn.gdeiassistant.Exception.PhoneException;

/**
 * 发送手机验证码失败时，抛出该异常
 */
public class SendSMSException extends Exception {

    public SendSMSException() {
    }

    public SendSMSException(String message) {
        super(message);
    }
}
