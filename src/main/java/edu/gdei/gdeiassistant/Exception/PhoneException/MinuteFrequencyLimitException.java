package edu.gdei.gdeiassistant.Exception.PhoneException;

/**
 * 分钟发送频率超过限制时，抛出该异常
 */
public class MinuteFrequencyLimitException extends SendSMSException {

    public MinuteFrequencyLimitException() {

    }

    public MinuteFrequencyLimitException(String message) {
        super(message);
    }
}
