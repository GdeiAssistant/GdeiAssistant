package edu.gdei.gdeiassistant.Service.QQ;

import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Pojo.Entity.QQUser;
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
public class QQAppService {

    private String appId;

    private String appSecret;
    @Autowired
    private RestTemplate restTemplate;

    @Value("#{propertiesReader['qq.app.appid']}")
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Value("#{propertiesReader['qq.app.secret']}")
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    /**
     * 使用登录凭证获取用户标识信息
     *
     * @param js_code
     * @return
     */
    public QQUser GetQQUser(String js_code) throws Exception {
        ResponseEntity<String> responseEntity = restTemplate.exchange("https://api.q.qq.com/sns/jscode2session?appid="
                        + appId + "&secret=" + appSecret + "&js_code=" + js_code + "&grant_type=authorization_code", HttpMethod.GET
                , new HttpEntity<>(new HttpHeaders()), String.class);
        JSONObject jsonObject = JSONObject.fromObject(responseEntity.getBody());
        if (jsonObject.has("openid")) {
            QQUser qqUser = new QQUser();
            qqUser.setOpenid(jsonObject.getString("openid"));
            if (jsonObject.has("unionid")) {
                qqUser.setUnionid(jsonObject.getString("unionid"));
            }
            return qqUser;
        }
        throw new ServerErrorException("获取QQ用户ID异常");
    }
}
