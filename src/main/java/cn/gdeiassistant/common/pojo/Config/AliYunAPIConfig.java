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
public class AliYunAPIConfig {

    @Autowired
    private ModuleUtils moduleUtils;

    private String official_appCode;

    public String getOfficial_appCode() {
        return official_appCode;
    }

    @Value("${api.aliyun.official.appcode:}")
    public void setOfficial_appCode(String official_appCode) {
        if (StringUtils.isNotBlank(official_appCode)) {
            this.official_appCode = official_appCode;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_API);
        }
    }
}
