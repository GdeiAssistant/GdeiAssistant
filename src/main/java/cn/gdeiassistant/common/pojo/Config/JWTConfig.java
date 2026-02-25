package cn.gdeiassistant.common.pojo.Config;

import cn.gdeiassistant.common.enums.Module.ModuleEnum;
import cn.gdeiassistant.common.tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
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

    @Value("${jwt.secret:}")
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
