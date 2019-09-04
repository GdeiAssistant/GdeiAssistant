package edu.gdei.gdeiassistant.Service.CloudAPI;

import edu.gdei.gdeiassistant.Exception.AuthenticationException.IDCardVerificationException;
import edu.gdei.gdeiassistant.Exception.RecognitionException.RecognitionException;
import edu.gdei.gdeiassistant.Pojo.Entity.Identity;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AliYunService {

    @Autowired
    private RestTemplate restTemplate;

    private String aliyunUserId;

    private String aliyun_h5_verifyKey;

    private String aliyun_h5_verifySecret;

    private String aliyun_mini_verifyKey;

    private String aliyun_mini_verifySecret;

    private String official_appCode;

    @Value("#{propertiesReader['api.aliyun.userid']}")
    public void setAliyunUserId(String aliyunUserId) {
        this.aliyunUserId = aliyunUserId;
    }

    @Value("#{propertiesReader['api.aliyun.h5.verifykey']}")
    public void setAliyun_h5_verifyKey(String aliyun_h5_verifyKey) {
        this.aliyun_h5_verifyKey = aliyun_h5_verifyKey;
    }

    @Value("#{propertiesReader['api.aliyun.h5.verifysecret']}")
    public void setAliyun_h5_verifySecret(String aliyun_h5_verifySecret) {
        this.aliyun_h5_verifySecret = aliyun_h5_verifySecret;
    }

    @Value("#{propertiesReader['api.aliyun.mini.verifykey']}")
    public void setAliyun_mini_verifyKey(String aliyun_mini_verifyKey) {
        this.aliyun_mini_verifyKey = aliyun_mini_verifyKey;
    }

    @Value("#{propertiesReader['api.aliyun.mini.verifysecret']}")
    public void setAliyun_mini_verifySecret(String aliyun_mini_verifySecret) {
        this.aliyun_mini_verifySecret = aliyun_mini_verifySecret;
    }

    @Value("#{propertiesReader['api.aliyun.official.appcode']}")
    public void setOfficial_appCode(String official_appCode) {
        this.official_appCode = official_appCode;
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
        httpHeaders.set("Authorization", "APPCODE " + official_appCode);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject configure = new JSONObject();
        configure.put("min_size", "16");
        configure.put("output_prob", false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("configure", configure);
        jsonObject.put("image", image);
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonObject.toString(), httpHeaders);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange("https://tysbgpu.market.alicloudapi.com/api/predict/ocr_general"
                , HttpMethod.POST, httpEntity, JSONObject.class);
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
     * 识别身份证图片的文字，获取身份证实名信息
     *
     * @return
     */
    public Identity ParseIdentityCard(String image) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + official_appCode);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject configure = new JSONObject();
        configure.put("side", "face");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("configure", configure);
        jsonObject.put("image", image);
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonObject.toString(), httpHeaders);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange("https://dm-51.data.aliyun.com/rest/160601/ocr/ocr_idcard.json"
                , HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject result = responseEntity.getBody();
        if (result.has("success") && result.getBoolean("success")) {
            //身份证校验通过，进行解析信息
            Identity identity = new Identity();
            identity.setName(result.getString("name"));
            identity.setCode(result.getString("num"));
            identity.setSex(result.getString("sex"));
            identity.setNation(result.getString("nationality"));
            identity.setAddress(result.getString("address"));
            identity.setBirthday(result.getString("birth"));
            return identity;
        }
        throw new RecognitionException("OCR识别失败");
    }

    /**
     * 身份二要素（姓名+身份证号码）实名制认证
     *
     * @param name
     * @param number
     * @return
     */
    public void VerifyIdentityCard(String name, String number) throws IDCardVerificationException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + official_appCode);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange("https://safrvcert.market.alicloudapi.com/" +
                "safrv_2meta_id_name/?__userId=" + aliyunUserId + "&identifyNum=" + number + "&userName=" + name +
                "&verifyKey=" + aliyun_h5_verifyKey, HttpMethod.GET, new HttpEntity<>(httpHeaders), JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        if (jsonObject.has("code") && jsonObject.getInt("code") == 200) {
            //校验成功
            return;
        }
        throw new IDCardVerificationException("身份证信息校验不通过");
    }

    /**
     * 身份三要素（姓名+身份证号码+手机号）实名制认证
     *
     * @param name
     * @param number
     * @return
     */
    public void VerifyIdentityCard(String name, String number, String phone) throws IDCardVerificationException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + official_appCode);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange("https://safrvcert.market.alicloudapi.com/" +
                        "safrv_3meta_id_name_phone/?__userId=" + aliyunUserId + "&identifyNum=" + number +
                        "&mobilePhone=" + phone + "&userName=" + name + "&verifyKey=" + aliyun_h5_verifyKey
                , HttpMethod.GET, new HttpEntity<>(httpHeaders), JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        if (jsonObject.has("code") && jsonObject.getInt("code") == 200) {
            //校验成功
            return;
        }
        throw new IDCardVerificationException("身份证信息校验不通过");
    }

    /**
     * 身份四要素（姓名+身份证号码+手机号+银行卡）实名制认证
     *
     * @param name
     * @param identityNumber
     * @param phone
     * @param cardNumber
     * @return
     */
    public void VerifyIdentityCard(String name, String identityNumber, String phone, String cardNumber) throws IDCardVerificationException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + official_appCode);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange("https://safrvcert.market.alicloudapi.com/" +
                        "safrv_3meta_id_name_phone/?__userId=" + aliyunUserId + "&identifyNum=" + identityNumber +
                        "&mobilePhone=" + phone + "&userName=" + name + "&bankCard=" + cardNumber + "&verifyKey=" + aliyun_h5_verifyKey
                , HttpMethod.GET, new HttpEntity<>(httpHeaders), JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        if (jsonObject.has("code") && jsonObject.getInt("code") == 200) {
            //校验成功
            return;
        }
        throw new IDCardVerificationException("身份证信息校验不通过");
    }
}
