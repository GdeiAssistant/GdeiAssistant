package com.linguancheng.gdeiassistant.Service.CloudAPI;

import com.linguancheng.gdeiassistant.Pojo.Entity.Location;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AliYunService {

    @Autowired
    private RestTemplate restTemplate;

    private String host;

    private String path;

    private String appcode;

    @Value("#{propertiesReader['api.aliyun.ipaddress.host']}")
    public void setHost(String host) {
        this.host = host;
    }

    @Value("#{propertiesReader['api.aliyun.ipaddress.path']}")
    public void setPath(String path) {
        this.path = path;
    }

    @Value("#{propertiesReader['api.aliyun.ipaddress.appcode']}")
    public void setAppcode(String appcode) {
        this.appcode = appcode;
    }

    /**
     * 根据IP地址查询IP地址归属地
     *
     * @param ip
     * @return
     */
    public Location GetLocationInfoByIPAddress(String ip) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + appcode);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(host + path + "?ip=" + ip, HttpMethod.GET
                , new HttpEntity<>(httpHeaders), JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        if (jsonObject.has("ret") && jsonObject.getInt("ret") == 200) {
            Location location = new Location();
            location.setArea(jsonObject.getJSONObject("data").getString("area"));
            location.setCity(jsonObject.getJSONObject("data").getString("city"));
            location.setCountry(jsonObject.getJSONObject("data").getString("country"));
            location.setRegion(jsonObject.getJSONObject("data").getString("region"));
            return location;
        }
        return null;
    }
}
