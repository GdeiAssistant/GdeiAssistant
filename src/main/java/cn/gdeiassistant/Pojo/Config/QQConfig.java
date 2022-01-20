package cn.gdeiassistant.Pojo.Config;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class QQConfig {

    private String appId;

    private String appSecret;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['qq.app.appid']}")
    public void setAppId(String appId) {
        if (StringUtils.isNotBlank(appId)) {
            this.appId = appId;
        } else {
            moduleUtils.DisableModule(ModuleEnum.QQ);
        }
    }

    @Value("#{propertiesReader['qq.app.appsecret']}")
    public void setAppSecret(String appSecret) {
        if (StringUtils.isNotBlank(appSecret)) {
            this.appSecret = appSecret;
        } else {
            moduleUtils.DisableModule(ModuleEnum.QQ);
        }
    }

    public String getAppId() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }
}
