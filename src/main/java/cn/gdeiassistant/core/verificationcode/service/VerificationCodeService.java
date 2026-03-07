package cn.gdeiassistant.core.verificationCode.service;

import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;
import cn.gdeiassistant.core.capability.sms.SmsVerificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeService {

    @Autowired
    private SmsVerificationSender smsVerificationSender;

    /**
     * 国内手机发送短信验证码
     *
     * @param code
     * @param phone
     */
    public void sendChinaPhoneVerificationCodeSms(int code, String phone) throws SendSMSException {
        smsVerificationSender.sendChina(code, phone);
    }

    /**
     * 港澳台和国际手机发送短信验证码
     *
     * @param code
     * @param areaCode
     * @param phone
     */
    public void sendGlobalPhoneVerificationCodeSms(int code, int areaCode, String phone) throws SendSMSException {
        smsVerificationSender.sendGlobal(code, areaCode, phone);
    }
}
