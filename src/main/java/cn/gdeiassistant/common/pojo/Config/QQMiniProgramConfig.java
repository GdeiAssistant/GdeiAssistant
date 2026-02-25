package cn.gdeiassistant.common.pojo.Config;

import cn.gdeiassistant.common.enums.Module.ModuleEnum;
import cn.gdeiassistant.common.tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

//@Component
//@Scope("singleton")
@Deprecated
public class QQMiniProgramConfig {

    private String appId;

    private String appSecret;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("${qq.app.appid:}")
    public void setAppId(String appId) {
        if (StringUtils.isNotBlank(appId)) {
            this.appId = appId;
        } else {
            moduleUtils.DisableModule(ModuleEnum.QQ_MINIPROGRAM);
        }
    }

    @Value("${qq.app.appsecret:}")
    public void setAppSecret(String appSecret) {
        if (StringUtils.isNotBlank(appSecret)) {
            this.appSecret = appSecret;
        } else {
            moduleUtils.DisableModule(ModuleEnum.QQ_MINIPROGRAM);
        }
    }

    public String getAppId() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }
}
