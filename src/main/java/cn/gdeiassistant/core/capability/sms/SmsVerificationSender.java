package cn.gdeiassistant.core.capability.sms;

import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;

public interface SmsVerificationSender {

    void sendChina(int code, String phone) throws SendSMSException;

    void sendGlobal(int code, int areaCode, String phone) throws SendSMSException;
}
