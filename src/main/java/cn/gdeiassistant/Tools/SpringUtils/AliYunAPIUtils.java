package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Exception.AuthenticationException.InconsistentAuthenticationException;
import cn.gdeiassistant.Exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.Pojo.Config.AliYunAPIConfig;
import cn.gdeiassistant.Pojo.Entity.Authentication;
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
    private AliYunAPIConfig aliyunAPIConfig;

    /**
     * 校验中国居民身份证信息
     *
     * @param authentication
     */
    public void VerifyMainLandChineseResidentIDCard(Authentication authentication) throws InconsistentAuthenticationException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + aliyunAPIConfig.getOfficial_appCode());
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
        httpHeaders.set("Authorization", "APPCODE " + aliyunAPIConfig.getOfficial_appCode());
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
}
