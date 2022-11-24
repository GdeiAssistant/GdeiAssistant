package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.Exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.Pojo.Config.JisuConfig;
import cn.gdeiassistant.Pojo.Entity.IPAddressRecord;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class JiSuAPIUtils {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JisuConfig jisuConfig;

    /**
     * 根据IP地址查询IP地址归属地和网络类型
     *
     * @param ip
     * @return
     */
    public IPAddressRecord GetInfoByIPAddress(String ip) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        params.add("ip", ip);
        params.add("appkey", jisuConfig.getAppkey());
        JSONObject jsonObject = restTemplate.postForObject(jisuConfig.getHost() + jisuConfig.getIpaddressPath()
                , new HttpEntity<>(params, httpHeaders), JSONObject.class);
        if (jsonObject.has("status") && jsonObject.getString("status").equals("0")) {
            IPAddressRecord ipAddressRecord = new IPAddressRecord();
            ipAddressRecord.setNetwork((jsonObject.getJSONObject("result").has("type") && !jsonObject.getJSONObject("result").get("type").equals("")) ? jsonObject.getJSONObject("result").getString("type") : null);
            ipAddressRecord.setCountry((jsonObject.getJSONObject("result").has("country") && !jsonObject.getJSONObject("result").get("country").equals("null")) ? jsonObject.getJSONObject("result").getString("country") : null);
            ipAddressRecord.setProvince((jsonObject.getJSONObject("result").has("province") && !jsonObject.getJSONObject("result").get("province").equals("null")) ? jsonObject.getJSONObject("result").getString("province") : null);
            ipAddressRecord.setCity((jsonObject.getJSONObject("result").has("city") && !jsonObject.getJSONObject("result").get("city").equals("null")) ? jsonObject.getJSONObject("result").getString("city") : null);
            return ipAddressRecord;
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
