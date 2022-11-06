package cn.gdeiassistant.Exception.VerificationException;

/**
 * 当用户输入的手机不合法时，抛出该异常
 */
public class IllegalPhoneNumberException extends SendSMSException {

    public IllegalPhoneNumberException() {

    }

    public IllegalPhoneNumberException(String message) {
        super(message);
    }
}
