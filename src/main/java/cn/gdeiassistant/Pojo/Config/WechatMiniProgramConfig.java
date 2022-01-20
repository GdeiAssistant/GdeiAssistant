package cn.gdeiassistant.Pojo.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class WechatMiniProgramConfig {

    private String appid;

    @Value("#{propertiesReader['wechat.account.appid']}")
    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppid() {
        return appid;
    }
}
