package cn.gdeiassistant.Exception.VerificationException;

/**
 * 发送电子邮件验证码失败时，抛出该异常
 */
public class SendEmailException extends Exception {

    public SendEmailException() {
    }

    public SendEmailException(String message) {
        super(message);
    }

}
