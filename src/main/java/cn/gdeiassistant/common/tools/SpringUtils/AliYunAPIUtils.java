package cn.gdeiassistant.common.tools.SpringUtils;

import cn.gdeiassistant.common.exception.AuthenticationException.InconsistentAuthenticationException;
import cn.gdeiassistant.common.exception.CommonException.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.pojo.Config.AliYunAPIConfig;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.common.pojo.Entity.Authentication;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AliYunAPIUtils {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AliYunAPIConfig aliyunAPIConfig;

    /**
     * 校验中国居民身份证信息
     *
     * @param authentication
     */
    public void VerifyMainLandChineseResidentIDCard(Authentication authentication) throws InconsistentAuthenticationException {
        if (StringUtils.isBlank(aliyunAPIConfig.getOfficial_appCode())) {
            throw new FeatureNotEnabledException("阿里云实名认证 API 未配置，无法校验身份证");
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
        if (StringUtils.isBlank(aliyunAPIConfig.getOfficial_appCode())) {
            throw new FeatureNotEnabledException("阿里云 OCR API 未配置，无法识别图片数字");
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
        throw new RecognitionException("OCR识别失败");
    }
}
