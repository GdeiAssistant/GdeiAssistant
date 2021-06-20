package cn.gdeiassistant.Exception.PhoneException;

/**
 * 缓存中没有验证码记录（过期或未申请）或验证码不匹配时，抛出该异常
 */
public class VerificationCodeInvalidException extends Exception {

    public VerificationCodeInvalidException() {
    }

    public VerificationCodeInvalidException(String message) {
        super(message);
    }
}
