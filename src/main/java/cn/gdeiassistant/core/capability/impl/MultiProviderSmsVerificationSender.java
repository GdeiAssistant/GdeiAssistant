package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;
import cn.gdeiassistant.core.capability.sms.SmsVerificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MultiProviderSmsVerificationSender implements SmsVerificationSender {

    @Autowired
    private SmsService smsService;

    @Override
    public void sendChina(int code, String phone) throws SendSMSException {
        smsService.sendChina(code, phone);
    }

    @Override
    public void sendGlobal(int code, int areaCode, String phone) throws SendSMSException {
        smsService.sendGlobal(code, areaCode, phone);
    }
}
