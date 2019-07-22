package edu.gdei.gdeiassistant.Service.CloudAPI;

import edu.gdei.gdeiassistant.Exception.AuthenticationException.IDCardVerificationException;
import edu.gdei.gdeiassistant.Pojo.Entity.Location;
import edu.gdei.gdeiassistant.Service.Authenticate.AuthenticateDataService;
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

    @Autowired
    private AuthenticateDataService authenticateDataService;

    private String aliyunUserId;

    private String aliyun_h5_verifyKey;

    private String aliyun_h5_verifySecret;

    private String aliyun_mini_verifyKey;

    private String aliyun_mini_verifySecret;

    private String ip_address_host;

    private String ip_address_path;

    private String ip_address_appCode;

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

    @Value("#{propertiesReader['api.aliyun.ipaddress.host']}")
    public void setIp_address_host(String ip_address_host) {
        this.ip_address_host = ip_address_host;
    }

    @Value("#{propertiesReader['api.aliyun.ipaddress.path']}")
    public void setIp_address_path(String ip_address_path) {
        this.ip_address_path = ip_address_path;
    }

    @Value("#{propertiesReader['api.aliyun.ipaddress.appcode']}")
    public void setIp_address_appCode(String ip_address_appCode) {
        this.ip_address_appCode = ip_address_appCode;
    }

    @Value("#{propertiesReader['api.aliyun.official.appcode']}")
    public void setOfficial_appCode(String official_appCode) {
        this.official_appCode = official_appCode;
    }

    /**
     * 根据IP地址查询IP地址归属地
     *
     * @param ip
     * @return
     */
    public Location GetLocationInfoByIPAddress(String ip) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + ip_address_appCode);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(ip_address_host
                        + ip_address_path + "?ip=" + ip, HttpMethod.GET
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
