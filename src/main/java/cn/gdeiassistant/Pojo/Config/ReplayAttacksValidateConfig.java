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
public class ReplayAttacksValidateConfig {

    private String token;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['request.validate.token']}")
    public void setToken(String token) {
        if(StringUtils.isNotBlank(token)){
            this.token = token;
        }
        else{
            moduleUtils.DisableModule(ModuleEnum.REPLAY_ATTACKS_VALIDATE);
        }
    }

    public String getToken() {
        return token;
    }
}
