package cn.gdeiassistant.common.exception.verificationexception;

/**
 * 当用户输入的手机不合法时，抛出该异常
 */
public class IllegalPhoneNumberException extends SendSMSException {

    public IllegalPhoneNumberException() {

    }

    public IllegalPhoneNumberException(String message) {
        super(message);
    }

    private IllegalPhoneNumberException(String key, Object[] args) {
        super(key, args, null);
    }

    public static IllegalPhoneNumberException ofKey(String key, Object... args) {
        return new IllegalPhoneNumberException(key, args);
    }
}
