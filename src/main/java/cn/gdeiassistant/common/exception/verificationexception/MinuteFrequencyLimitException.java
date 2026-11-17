package cn.gdeiassistant.common.exception.verificationexception;

/**
 * 分钟发送频率超过限制时，抛出该异常
 */
public class MinuteFrequencyLimitException extends SendSMSException {

    public MinuteFrequencyLimitException() {

    }

    public MinuteFrequencyLimitException(String message) {
        super(message);
    }

    private MinuteFrequencyLimitException(String key, Object[] args) {
        super(key, args, null);
    }

    public static MinuteFrequencyLimitException ofKey(String key, Object... args) {
        return new MinuteFrequencyLimitException(key, args);
    }
}
