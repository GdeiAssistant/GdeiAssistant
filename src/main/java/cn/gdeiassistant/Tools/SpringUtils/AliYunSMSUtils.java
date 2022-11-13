package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Exception.VerificationException.*;
import cn.gdeiassistant.Pojo.Config.AliYunSMSConfig;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AliYunSMSUtils {

    @Autowired
    private AliYunSMSConfig aliyunSMSConfig;

    /**
     * 国内手机发送短信验证码
     *
     * @param code
     * @param phone
     */
    public void SendChinaPhoneVerificationCodeSMS(int code, String phone) throws ClientException, SendSMSException {
        DefaultProfile profile = DefaultProfile.getProfile("default", aliyunSMSConfig.getSmsAliyunAccessKeyId()
                , aliyunSMSConfig.getSmsAliyunAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "default");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", "广东二师助手");
        request.putQueryParameter("TemplateCode", aliyunSMSConfig.getSmsAliyunChinaTemplateCode());
        request.putQueryParameter("TemplateParam", "{code:\"" + code + "\"}");
        CommonResponse response = client.getCommonResponse(request);
        JSONObject result = JSONObject.fromObject(response.getData());
        if (result.containsKey("Code")) {
            if (result.getString("Code").equals("OK")) {
                //发送成功
            } else if (result.getString("Code").equals("isv.MOBILE_NUMBER_ILLEGAL")) {
                //不合法的手机号
                throw new IllegalPhoneNumberException();
            } else if (result.getString("Code").equals("VALVE:M_MC")) {
                //分钟请求次数过多
                throw new MinuteFrequencyLimitException();
            } else if (result.getString("Code").equals("VALVE:H_MC")) {
                //小时请求次数过多
                throw new HourFrequencyLimitException();
            } else if (result.getString("Code").equals("VALVE:D_MC")) {
                //天请求次数过多
                throw new DayFrequencyLimitException();
            } else {
                //其他错误
                throw new SendSMSException();
            }
        }
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
        DefaultProfile profile = DefaultProfile.getProfile("default", aliyunSMSConfig.getSmsAliyunAccessKeyId()
                , aliyunSMSConfig.getSmsAliyunAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "default");
        request.putQueryParameter("PhoneNumbers", areaCode + phone);
        request.putQueryParameter("SignName", "广东二师助手");
        request.putQueryParameter("TemplateCode", aliyunSMSConfig.getSmsAliyunGlobalTemplateCode());
        request.putQueryParameter("TemplateParam", "{code:\"" + code + "\"}");
        CommonResponse response = client.getCommonResponse(request);
        JSONObject result = JSONObject.fromObject(response.getData());
        if (result.containsKey("Code")) {
            if (result.getString("Code").equals("OK")) {
                //发送成功
            } else if (result.getString("Code").equals("isv.MOBILE_NUMBER_ILLEGAL")) {
                //不合法的手机号
                throw new IllegalPhoneNumberException();
            } else if (result.getString("Code").equals("VALVE:M_MC")) {
                //分钟请求次数过多
                throw new MinuteFrequencyLimitException();
            } else if (result.getString("Code").equals("VALVE:H_MC")) {
                //小时请求次数过多
                throw new HourFrequencyLimitException();
            } else if (result.getString("Code").equals("VALVE:D_MC")) {
                //天请求次数过多
                throw new DayFrequencyLimitException();
            } else {
                //其他错误
                throw new SendSMSException();
            }
        }
    }
}
