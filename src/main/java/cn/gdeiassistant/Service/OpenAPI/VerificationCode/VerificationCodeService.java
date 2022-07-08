package cn.gdeiassistant.Service.OpenAPI.VerificationCode;

import cn.gdeiassistant.Exception.PhoneException.SendSMSException;
import cn.gdeiassistant.Tools.SpringUtils.AliYunAPIUtils;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeService {

    @Autowired
    private AliYunAPIUtils aliYunAPIUtils;

    /**
     * 国内手机发送短信验证码
     *
     * @param code
     * @param phone
     */
    public void SendChinaPhoneVerificationCodeSMS(int code, String phone) throws ClientException, SendSMSException {
        aliYunAPIUtils.SendChinaPhoneVerificationCodeSMS(code, phone);
    }

    /**
     * 港澳台和国际手机发送短信验证码
     *
     * @param code
     * @param areaCode
     * @param phone
     * @throws ClientException
     */
    public void SendGlobalPhoneVerificationCodeSMS(int code, int areaCode, String phone) throws ClientException, SendSMSException {
        aliYunAPIUtils.SendGlobalPhoneVerificationCodeSMS(code, areaCode, phone);
    }
}
