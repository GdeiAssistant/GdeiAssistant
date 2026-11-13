package cn.gdeiassistant.common.tools.springutils;

import cn.gdeiassistant.common.exception.authenticationexception.InconsistentAuthenticationException;
import cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.common.pojo.config.AliYunAPIConfig;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import cn.gdeiassistant.common.pojo.entity.Authentication;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import cn.gdeiassistant.common.i18n.Messages;

@Component
public class AliYunAPIUtils {

    private final RestTemplate restTemplate;

    private final AliYunAPIConfig aliyunAPIConfig;

    public AliYunAPIUtils(RestTemplate restTemplate, AliYunAPIConfig aliyunAPIConfig) {
        this.restTemplate = restTemplate;
        this.aliyunAPIConfig = aliyunAPIConfig;
    }

    /**
     * 校验中国居民身份证信息
     *
     * @param authentication
     */
    public void verifyMainLandChineseResidentIDCard(Authentication authentication) throws InconsistentAuthenticationException {
        if (StringUtils.isBlank(aliyunAPIConfig.getOfficial_appCode())) {
            throw new FeatureNotEnabledException(Messages.get("common.tools.aliyunIdVerificationNotConfigured"));
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + aliyunAPIConfig.getOfficial_appCode());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("idcard", authentication.getNumber());
        jsonObject.put("name", authentication.getName());
        ResponseEntity<String> responseEntity = restTemplate
                .exchange("https://eid.shumaidata.com/eid/check"
                        , HttpMethod.POST, new HttpEntity<>(jsonObject.toJSONString(), httpHeaders), String.class);
        JSONObject result = JSON.parseObject(responseEntity.getBody());
        if (Integer.valueOf(result.getIntValue("code")).equals(0)) {
            if (Integer.valueOf(result.getJSONObject("result").getIntValue("res")).equals(1)) {
                //校验通过
                return;
            }
            throw new InconsistentAuthenticationException(Messages.get("common.tools.verificationFailed"));
        }
    }

    /**
     * OCR识别图片中的数字，返回数字文本串
     *
     * @param image
     * @return
     * @throws RecognitionException
     */
    public String characterNumberRecognize(String image) throws RecognitionException {
        if (StringUtils.isBlank(aliyunAPIConfig.getOfficial_appCode())) {
            throw new FeatureNotEnabledException(Messages.get("common.tools.aliyunOcrNotConfigured"));
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + aliyunAPIConfig.getOfficial_appCode());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject configure = new JSONObject();
        configure.put("min_size", "16");
        configure.put("output_prob", false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("configure", configure);
        jsonObject.put("image", image);
        ResponseEntity<String> responseEntity = restTemplate.exchange(aliyunAPIConfig.getOcrGeneralEndpoint()
                , HttpMethod.POST, new HttpEntity<>(jsonObject.toJSONString(), httpHeaders), String.class);
        JSONObject result = JSON.parseObject(responseEntity.getBody());
        if (result.containsKey("success") && result.getBoolean("success")) {
            JSONArray jsonArray = result.getJSONArray("ret");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < jsonArray.size(); i++) {
                String words = jsonArray.getJSONObject(i).getString("word");
                if (words.matches("^[0-9]*$")) {
                    stringBuilder.append(words);
                }
            }
            return stringBuilder.toString();
        }
        throw new RecognitionException(Messages.get("common.tools.ocrFailed"));
    }
}
