package com.linguancheng.gdeiassistant.Service.Recognition;

import com.linguancheng.gdeiassistant.Enum.Recognition.RecognitionEnum;
import com.linguancheng.gdeiassistant.Exception.RecognitionException.RecognitionException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecognitionService {

    @Autowired
    private RestTemplate restTemplate;

    private String grant_type;

    private String client_id;

    private String client_secret;

    private String aliyun_appcode;

    @Value("#{propertiesReader['baidu.type']}")
    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    @Value("#{propertiesReader['baidu.id']}")
    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    @Value("#{propertiesReader['baidu.secret']}")
    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    @Value("#{propertiesReader['aliyun.appcode']}")
    public void setAliyun_appcode(String aliyun_appcode) {
        this.aliyun_appcode = aliyun_appcode;
    }

    public enum CheckCodeTypeEnum {

        NUMBER("n"), ENGLISH_WITH_NUMBER("en");

        CheckCodeTypeEnum(String type) {
            this.type = type;
        }

        private String type;

        public String getType() {
            return type;
        }
    }

    /**
     * 获取权限访问令牌
     *
     * @return
     * @throws RecognitionException
     */
    private String GetAccessToken() throws RecognitionException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", grant_type);
        params.add("client_id", client_id);
        params.add("client_secret", client_secret);
        JSONObject jsonObject = restTemplate
                .postForObject("https://aip.baidubce.com/oauth/2.0/token", params, JSONObject.class);
        if (jsonObject.has("access_token")) {
            return jsonObject.getString("access_token");
        }
        throw new RecognitionException("获取权限访问令牌失败");
    }

    /**
     * 注册人脸库信息
     *
     * @param image
     * @return
     */
    public RecognitionEnum FaceSetRegister(String username, String image) throws RecognitionException {
        String token = GetAccessToken();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("uid", username);
        params.add("group_id", "gdeiassistant");
        params.add("user_info", username);
        params.add("action_type", "replace");
        params.add("image", image);
        JSONObject jsonObject = restTemplate.postForObject("https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add?access_token="
                + token, params, JSONObject.class);
        if (jsonObject.has("error_code")) {
            //注册人脸信息错误
            switch (jsonObject.getInt("error_code")) {
                case 216402:
                    //未发现人脸
                    return RecognitionEnum.FACE_NOT_FOUND;

                default:
                    //服务器异常
                    return RecognitionEnum.SERVER_ERROR;
            }
        }
        return RecognitionEnum.SUCCESS;
    }

    /**
     * 人脸认证
     *
     * @return
     */
    public RecognitionEnum FaceVerify(String username, String image) throws RecognitionException {
        String token = GetAccessToken();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("uid", username);
        params.add("image", image);
        params.add("group_id", "gdeiassistant");
        JSONObject jsonObject = restTemplate.postForObject("https://aip.baidubce.com/rest/2.0/face/v2/verify?access_token="
                + token, params, JSONObject.class);
        if (jsonObject.has("error_code")) {
            //注册人脸信息错误
            switch (jsonObject.getInt("error_code")) {
                case 216402:
                    //未发现人脸
                    return RecognitionEnum.FACE_NOT_FOUND;

                default:
                    //服务器异常
                    return RecognitionEnum.SERVER_ERROR;
            }
        }
        if (jsonObject.getJSONArray("result").getInt(0) >= 70) {
            return RecognitionEnum.SUCCESS;
        }
        return RecognitionEnum.FAILURE;
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

    /**
     * OCR识别图片中的数字，返回数字文本串
     *
     * @param image
     * @return
     */
    public String CharacterNumberRecognize(String image) throws RecognitionException {
        String token = GetAccessToken();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("image", image);
        params.add("detect_direction", "false");
        params.add("detect_language", "false");
        JSONObject jsonObject = restTemplate.postForObject("https://aip.baidubce.com/rest/2.0/ocr/v1/webimage?access_token="
                + token, params, JSONObject.class);
        if (jsonObject.has("log_id") && jsonObject.has("words_result")) {
            JSONArray jsonArray = jsonObject.getJSONArray("words_result");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < jsonArray.size(); i++) {
                String words = ((JSONObject) jsonArray.get(i)).getString("words");
                if (words.matches("^[0-9]*$")) {
                    stringBuilder.append(words);
                }
            }
            return stringBuilder.toString();
        }
        throw new RecognitionException("OCR识别失败");
    }
}
