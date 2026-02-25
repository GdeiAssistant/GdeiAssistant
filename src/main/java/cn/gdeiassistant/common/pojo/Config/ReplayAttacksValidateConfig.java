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
public class ReplayAttacksValidateConfig {

    private String token;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("${request.validate.token:}")
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
