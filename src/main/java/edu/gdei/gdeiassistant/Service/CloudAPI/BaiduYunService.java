package edu.gdei.gdeiassistant.Service.CloudAPI;

import edu.gdei.gdeiassistant.Enum.Recognition.RecognitionEnum;
import edu.gdei.gdeiassistant.Exception.AuthenticationException.*;
import edu.gdei.gdeiassistant.Exception.RecognitionException.RecognitionException;
import edu.gdei.gdeiassistant.Pojo.Entity.Identity;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class BaiduYunService {

    @Autowired
    private RestTemplate restTemplate;

    private String grantType;

    private String clientId;

    private String clientSecret;

    @Value("#{propertiesReader['baidu.grant.type']}")
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    @Value("#{propertiesReader['baidu.client.id']}")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Value("#{propertiesReader['baidu.client.secret']}")
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * 获取百度云接口权限访问令牌
     *
     * @return
     * @throws RecognitionException
     */
    private String GetBaiduAccessToken() throws RecognitionException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", grantType);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        JSONObject jsonObject = restTemplate
                .postForObject("https://aip.baidubce.com/oauth/2.0/token", params, JSONObject.class);
        if (jsonObject.has("access_token")) {
            return jsonObject.getString("access_token");
        }
        throw new RecognitionException("获取权限访问令牌失败");
    }

    /**
     * 识别身份证图片的文字，获取身份证实名信息
     *
     * @return
     */
    public Identity ParseIdentityCard(String image) throws Exception {
        String token = GetBaiduAccessToken();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id_card_side", "front");
        params.add("image", image);
        params.add("detect_risk", "true");
        JSONObject jsonObject = restTemplate.postForObject("https://aip.baidubce.com/rest/2.0/ocr/v1/idcard?access_token="
                + token, params, JSONObject.class);
        if (jsonObject.has("log_id") && jsonObject.has("words_result")) {
            if (jsonObject.has("edit_tool")) {
                //身份证照片被编辑软件编辑过
                throw new IDCardEditedException("身份证照片被编辑过");
            }
            //获取身份证图片识别状态
            switch (jsonObject.getString("image_status")) {
                case "normal":
                    //获取身份证类型
                    switch (jsonObject.getString("risk_type")) {
                        case "normal":
                            //身份证校验通过，进行解析信息
                            JSONObject wordsResult = jsonObject.getJSONObject("words_result");
                            Identity identity = new Identity();
                            identity.setName(wordsResult.getJSONObject("姓名").getString("words"));
                            identity.setCode(wordsResult.getJSONObject("公民身份号码").getString("words"));
                            identity.setSex(wordsResult.getJSONObject("性别").getString("words"));
                            identity.setNation(wordsResult.getJSONObject("民族").getString("words"));
                            identity.setAddress(wordsResult.getJSONObject("住址").getString("words"));
                            identity.setBirthday(wordsResult.getJSONObject("出生").getString("words"));
                            return identity;

                        case "copy":
                            throw new IDCardCopyTypeException("身份证图片为复印件");

                        case "temporary":
                            throw new IDCardTemporaryTypeException("身份证图片为临时身份证");

                        case "screen":
                            throw new IDCardScreenTypeException("身份证图片为翻拍");

                        case "unknown":
                        default:
                            throw new RecognitionException("OCR识别失败");
                    }

                case "reversed_side":
                    throw new IDCardReversedSideException("身份证正反面颠倒");

                case "non_idcard":
                    throw new NonIDCardInfoException("上传的图片中不包含身份证");

                case "blurred":
                    throw new IDCardBlurredException("身份证模糊");

                case "other_type_card":
                    throw new OtherTypeCardException("其他类型证照");

                case "over_exposure":
                    throw new IDCardOverExposureException("身份证关键字段反光或过曝");

                case "unknown":
                default:
                    throw new RecognitionException("OCR识别失败");
            }
        }
        throw new RecognitionException("OCR识别失败");
    }

    /**
     * 注册人脸库信息
     *
     * @param username
     * @param image
     * @return
     */
    public RecognitionEnum FaceSetRegister(String username, String image) throws RecognitionException {
        String token = GetBaiduAccessToken();
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
     * @param username
     * @param image
     * @return
     * @throws RecognitionException
     */
    public RecognitionEnum FaceVerify(String username, String image) throws RecognitionException {
        String token = GetBaiduAccessToken();
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
     * 高精度识别验证码图片中的文字，返回文本串
     *
     * @param image
     * @return
     * @throws RecognitionException
     */
    public String AccurateCharacterRecognize(String image) throws RecognitionException {
        String token = GetBaiduAccessToken();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("image", image);
        params.add("detect_direction", "false");
        params.add("probability", "false");
        JSONObject jsonObject = restTemplate.postForObject("https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token="
                + token, params, JSONObject.class);
        if (jsonObject.has("log_id") && jsonObject.has("words_result")) {
            JSONArray jsonArray = jsonObject.getJSONArray("words_result");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < jsonArray.size(); i++) {
                String words = jsonArray.getJSONObject(i).getString("words");
                stringBuilder.append(words);
            }
            return stringBuilder.toString();
        }
        throw new RecognitionException("OCR识别失败");
    }

    /**
     * OCR识别图片中的数字，返回数字文本串
     *
     * @param image
     * @return
     * @throws RecognitionException
     */
    public String CharacterNumberRecognize(String image) throws RecognitionException {
        String token = GetBaiduAccessToken();
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
