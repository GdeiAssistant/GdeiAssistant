package com.linguancheng.gdeiassistant.Service.CloudAPI;

import com.linguancheng.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import com.linguancheng.gdeiassistant.Exception.RecognitionException.RecognitionException;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AliYunService {

    @Autowired
    private RestTemplate restTemplate;

    private String aliyun_appcode;

    @Value("#{propertiesReader['aliyun.appcode']}")
    public void setAliyun_appcode(String aliyun_appcode) {
        this.aliyun_appcode = aliyun_appcode;
    }

    /**
     * 神经网络识别验证码图片，返回验证码
     *
     * @param image
     * @param checkCodeTypeEnum
     * @param length
     * @return
     */
    public String CheckCodeRecognize(String image, CheckCodeTypeEnum checkCodeTypeEnum, int length) throws RecognitionException {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add("Authorization", "APPCODE " + aliyun_appcode);
        params.add("pic", image);
        params.add("type", checkCodeTypeEnum.getType() + length);
        JSONObject jsonObject = JSONObject.fromObject(restTemplate.postForObject("http://jisuyzmsb.market.alicloudapi.com/captcha/recognize"
                , new HttpEntity<>(params, httpHeaders), String.class));
        if (jsonObject.getString("status").equals("0")) {
            return jsonObject.getJSONObject("result").getString("code");
        }
        throw new RecognitionException("识别验证码图片失败");
    }
}
