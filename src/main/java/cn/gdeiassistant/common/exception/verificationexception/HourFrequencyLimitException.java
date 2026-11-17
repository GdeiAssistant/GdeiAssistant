package cn.gdeiassistant.common.exception.verificationexception;

/**
 * 小时发送频率超过限制时，抛出该异常
 */
public class HourFrequencyLimitException extends SendSMSException {

    public HourFrequencyLimitException() {
    }

    public HourFrequencyLimitException(String message) {
        super(message);
    }

    private HourFrequencyLimitException(String key, Object[] args) {
        super(key, args, null);
    }

    public static HourFrequencyLimitException ofKey(String key, Object... args) {
        return new HourFrequencyLimitException(key, args);
    }
}
