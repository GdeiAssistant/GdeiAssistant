package cn.gdeiassistant.Service.CloudAPI;

import cn.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.Exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.Pojo.Config.JisuConfig;
import cn.gdeiassistant.Pojo.Entity.Location;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class JiSuAPIService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JisuConfig jisuConfig;

    /**
     * 根据IP地址查询IP地址归属地
     *
     * @param ip
     * @return
     */
    public Location GetLocationInfoByIPAddress(String ip) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        params.add("ip", ip);
        params.add("appkey", jisuConfig.getAppkey());
        JSONObject jsonObject = restTemplate.postForObject(jisuConfig.getHost() + jisuConfig.getIpaddressPath()
                , new HttpEntity<>(params, httpHeaders), JSONObject.class);
        if (jsonObject.has("status") && jsonObject.getString("status").equals("0")) {
            Location location = new Location();
            location.setCountry(jsonObject.getJSONObject("result").has("country") ? jsonObject.getJSONObject("result").getString("country") : null);
            location.setProvince(jsonObject.getJSONObject("result").has("province") ? jsonObject.getJSONObject("result").getString("province") : null);
            location.setCity(jsonObject.getJSONObject("result").has("city") ? jsonObject.getJSONObject("result").getString("city") : null);
            return location;
        }
        return null;
    }

    /**
     * 极速数据API识别验证码图片，返回验证码
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
        params.add("pic", image);
        params.add("type", checkCodeTypeEnum.getType() + length);
        params.add("appkey", jisuConfig.getAppkey());
        JSONObject jsonObject = restTemplate.postForObject(jisuConfig.getHost() + jisuConfig.getRecognitionPath()
                , new HttpEntity<>(params, httpHeaders), JSONObject.class);
        if (jsonObject.has("status") && jsonObject.getString("status").equals("0")) {
            return jsonObject.getJSONObject("result").getString("code");
        }
        throw new RecognitionException("识别验证码图片失败");
    }
}
