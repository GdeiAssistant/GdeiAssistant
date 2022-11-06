package cn.gdeiassistant.Exception.VerificationException;

/**
 * 天数发送频率超过限制时，抛出该异常
 */
public class DayFrequencyLimitException extends SendSMSException {

    public DayFrequencyLimitException() {

    }

    public DayFrequencyLimitException(String message) {
        super(message);
    }
}
