package cn.gdeiassistant.common.exception.verificationexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 缓存中没有验证码记录（过期或未申请）或验证码不匹配时，抛出该异常
 */
public class VerificationCodeInvalidException extends KeyedBusinessException {

    public VerificationCodeInvalidException() {
    }

    public VerificationCodeInvalidException(String message) {
        super(message);
    }

    private VerificationCodeInvalidException(String key, Object[] args) {
        super(key, args, null);
    }

    public static VerificationCodeInvalidException ofKey(String key, Object... args) {
        return new VerificationCodeInvalidException(key, args);
    }
}
