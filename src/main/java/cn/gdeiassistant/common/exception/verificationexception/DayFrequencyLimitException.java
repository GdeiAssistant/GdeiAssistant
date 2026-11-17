package cn.gdeiassistant.common.exception.verificationexception;

/**
 * 天数发送频率超过限制时，抛出该异常
 */
public class DayFrequencyLimitException extends SendSMSException {

    public DayFrequencyLimitException() {

    }

    public DayFrequencyLimitException(String message) {
        super(message);
    }

    private DayFrequencyLimitException(String key, Object[] args) {
        super(key, args, null);
    }

    public static DayFrequencyLimitException ofKey(String key, Object... args) {
        return new DayFrequencyLimitException(key, args);
    }
}
