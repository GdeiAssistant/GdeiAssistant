package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Exception.AuthenticationException.InconsistentAuthenticationException;
import cn.gdeiassistant.Exception.VerificationException.*;
import cn.gdeiassistant.Exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.Pojo.Config.AliyunConfig;
import cn.gdeiassistant.Pojo.Entity.Authentication;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AliYunAPIUtils {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AliyunConfig aliyunConfig;

    /**
     * 校验中国居民身份证信息
     *
     * @param authentication
     */
    public void VerifyMainLandChineseResidentIDCard(Authentication authentication) throws InconsistentAuthenticationException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + aliyunConfig.getOfficial_appCode());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("idcard", authentication.getNumber());
        jsonObject.put("name", authentication.getName());
        ResponseEntity<JSONObject> responseEntity = restTemplate
                .exchange("https://eid.shumaidata.com/eid/check"
                        , HttpMethod.POST, new HttpEntity<>(jsonObject, httpHeaders), JSONObject.class);
        JSONObject result = responseEntity.getBody();
        if (Integer.valueOf(result.getInt("code")).equals(0)) {
            if (Integer.valueOf(result.getJSONObject("result").getInt("res")).equals(1)) {
                //校验通过
                return;
            }
            throw new InconsistentAuthenticationException("校验不通过");
        }
    }

    /**
     * OCR识别图片中的数字，返回数字文本串
     *
     * @param image
     * @return
     * @throws RecognitionException
     */
    public String CharacterNumberRecognize(String image) throws RecognitionException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + aliyunConfig.getOfficial_appCode());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject configure = new JSONObject();
        configure.put("min_size", "16");
        configure.put("output_prob", false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("configure", configure);
        jsonObject.put("image", image);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange("http://tysbgpu.market.alicloudapi.com/api/predict/ocr_general"
                , HttpMethod.POST, new HttpEntity<>(jsonObject, httpHeaders), JSONObject.class);
        JSONObject result = responseEntity.getBody();
        if (result.containsKey("success") && jsonObject.getBoolean("success")) {
            JSONArray jsonArray = jsonObject.getJSONArray("ret");
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : jsonArray) {
                String words = ((JSONObject) o).getString("word");
                if (words.matches("^[0-9]*$")) {
                    stringBuilder.append(words);
                }
            }
            return stringBuilder.toString();
        }
        throw new RecognitionException("OCR识别失败");
    }

    /**
     * 国内手机发送短信验证码
     *
     * @param code
     * @param phone
     */
    public void SendChinaPhoneVerificationCodeSMS(int code, String phone) throws ClientException, SendSMSException {
        DefaultProfile profile = DefaultProfile.getProfile("default", aliyunConfig.getAliyunAccessKeyId()
                , aliyunConfig.getAliyunAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "default");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", "广东二师助手");
        request.putQueryParameter("TemplateCode", aliyunConfig.getAliyunSMSChinaTemplateCode());
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
        DefaultProfile profile = DefaultProfile.getProfile("default", aliyunConfig.getAliyunAccessKeyId()
                , aliyunConfig.getAliyunAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "default");
        request.putQueryParameter("PhoneNumbers", areaCode + phone);
        request.putQueryParameter("SignName", "广东二师助手");
        request.putQueryParameter("TemplateCode", aliyunConfig.getAliyunSMSGlobalTemplateCode());
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
