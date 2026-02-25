package cn.gdeiassistant.common.tools.SpringUtils;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.CommonException.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.pojo.Config.JisuConfig;
import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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
     * 根据IP地址查询IP地址归属地和网络类型；未配置 AppKey 时返回 "-" 占位。
     */
    public IPAddressRecord getInfoByIPAddress(String ip) {
        if (jisuConfig == null || StringUtils.isBlank(jisuConfig.getAppkey())
                || StringUtils.isBlank(jisuConfig.getHost()) || StringUtils.isBlank(jisuConfig.getIpaddressPath())) {
            IPAddressRecord record = new IPAddressRecord();
            record.setNetwork("-");
            record.setCountry("-");
            record.setProvince("-");
            record.setCity("-");
            record.setArea("-");
            return record;
        }
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        params.add("ip", ip);
        params.add("appkey", jisuConfig.getAppkey());
        String responseBody = restTemplate.postForObject(jisuConfig.getHost() + jisuConfig.getIpaddressPath()
                , new HttpEntity<>(params, httpHeaders), String.class);
        JSONObject jsonObject = responseBody != null ? JSON.parseObject(responseBody) : null;
        if (jsonObject != null && jsonObject.containsKey("status") && "0".equals(jsonObject.getString("status"))) {
            IPAddressRecord ipAddressRecord = new IPAddressRecord();
            JSONObject res = jsonObject.getJSONObject("result");
            ipAddressRecord.setNetwork((res != null && res.containsKey("type") && !"".equals(res.get("type"))) ? res.getString("type") : null);
            ipAddressRecord.setCountry((res != null && res.containsKey("country") && !"null".equals(res.get("country"))) ? res.getString("country") : null);
            ipAddressRecord.setProvince((res != null && res.containsKey("province") && !"null".equals(res.get("province"))) ? res.getString("province") : null);
            ipAddressRecord.setCity((res != null && res.containsKey("city") && !"null".equals(res.get("city"))) ? res.getString("city") : null);
            return ipAddressRecord;
        }
        return null;
    }

    /**
     * 极速数据API识别验证码图片，返回验证码；未配置 AppKey 时抛出 FeatureNotEnabledException。
     */
    public String CheckCodeRecognize(String image, CheckCodeTypeEnum checkCodeTypeEnum, int length) throws RecognitionException {
        if (jisuConfig == null || StringUtils.isBlank(jisuConfig.getAppkey())
                || StringUtils.isBlank(jisuConfig.getHost()) || StringUtils.isBlank(jisuConfig.getRecognitionPath())) {
            throw new FeatureNotEnabledException("验证码识别未开启，无法使用该功能");
        }
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        params.add("pic", image);
        params.add("type", checkCodeTypeEnum.getType() + length);
        params.add("appkey", jisuConfig.getAppkey());
        String responseBody = restTemplate.postForObject(jisuConfig.getHost() + jisuConfig.getRecognitionPath()
                , new HttpEntity<>(params, httpHeaders), String.class);
        JSONObject jsonObject = responseBody != null ? JSON.parseObject(responseBody) : null;
        if (jsonObject != null && jsonObject.containsKey("status") && "0".equals(jsonObject.getString("status"))) {
            return jsonObject.getJSONObject("result").getString("code");
        }
        throw new RecognitionException("识别验证码图片失败");
    }
}
