package cn.gdeiassistant.Exception.VerificationException;

/**
 * 小时发送频率超过限制时，抛出该异常
 */
public class HourFrequencyLimitException extends SendSMSException {

    public HourFrequencyLimitException() {
    }

    public HourFrequencyLimitException(String message) {
        super(message);
    }
}
