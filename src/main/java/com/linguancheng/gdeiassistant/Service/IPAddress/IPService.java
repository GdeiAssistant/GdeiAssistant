package com.linguancheng.gdeiassistant.Service.IPAddress;

import com.linguancheng.gdeiassistant.Pojo.Entity.Location;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/1
 */

@Service
public class IPService {

    @Autowired
    private RestTemplate restTemplate;

    private String host;

    private String path;

    private String appcode;

    @Value("#{propertiesReader['api.ipaddress.host']}")
    public void setHost(String host) {
        this.host = host;
    }

    @Value("#{propertiesReader['api.ipaddress.path']}")
    public void setPath(String path) {
        this.path = path;
    }

    @Value("#{propertiesReader['api.ipaddress.appcode']}")
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
        ip = "219.136.198.242";
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

    /**
     * 通过Request请求获取IP地址
     *
     * @param request
     * @return
     */
    public String GetRequestRealIPAddress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if (index != -1) {
                return XFor.substring(0, index);
            } else {
                return XFor;
            }
        }
        XFor = Xip;
        if (StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            return XFor;
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }

}
