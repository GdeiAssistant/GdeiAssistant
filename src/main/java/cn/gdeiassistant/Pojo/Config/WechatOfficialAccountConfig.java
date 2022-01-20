package cn.gdeiassistant.Pojo.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class WechatOfficialAccountConfig {

    private String appid;

    private String appsecret;

    @Value("#{propertiesReader['wechat.account.appid']}")
    public void setAppid(String appid) {
        this.appid = appid;
    }

    @Value("#{propertiesReader['wechat.account.appsecret']}")
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
