package cn.gdeiassistant.Pojo.Config;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class WechatOfficialAccountConfig {

    private String appid;

    private String appsecret;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret;
    }

    public String getAppid() {
        return appid;
    }

    public String getAppsecret() {
        return appsecret;
    }
}
