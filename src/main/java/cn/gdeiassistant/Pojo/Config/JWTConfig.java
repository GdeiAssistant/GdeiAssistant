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
public class JWTConfig {

    private String secret;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['jwt.secret']}")
    public void setSecret(String secret) {
        if (StringUtils.isNotBlank(secret)) {
            this.secret = secret;
        } else {
            moduleUtils.DisableModule(ModuleEnum.JWT);
        }
    }

    public String getSecret() {
        return secret;
    }
}
