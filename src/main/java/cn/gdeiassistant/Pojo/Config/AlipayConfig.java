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
public class AlipayConfig {

    @Autowired
    private ModuleUtils moduleUtils;

    private String appid;

    private String publicKey;

    private String privateKey;

    @Value("#{propertiesReader['alipay.app.appid']}")
    public void setAppid(String appid) {
        if (StringUtils.isNotBlank(appid)) {
            this.appid = appid;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIPAY);
        }
    }

    @Value("#{propertiesReader['alipay.app.rsa2.privatekey']}")
    public void setPrivateKey(String privateKey) {
        if (StringUtils.isNotBlank(privateKey)) {
            this.privateKey = privateKey;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIPAY);
        }
    }

    @Value("#{propertiesReader['alipay.app.rsa2.publickey']}")
    public void setPublicKey(String publicKey) {
        if (StringUtils.isNotBlank(publicKey)) {
            this.publicKey = publicKey;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIPAY);
        }
    }

    public String getAppid() {
        return appid;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }
}
