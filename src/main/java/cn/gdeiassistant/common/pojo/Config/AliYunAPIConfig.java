package cn.gdeiassistant.common.pojo.config;

import cn.gdeiassistant.common.enums.module.ModuleEnum;
import cn.gdeiassistant.common.tools.springutils.ModuleUtils;
import cn.gdeiassistant.common.tools.utils.StringUtils;
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

    private String ocrGeneralEndpoint = "https://tysbgpu.market.alicloudapi.com/api/predict/ocr_general";

    public String getOcrGeneralEndpoint() {
        return ocrGeneralEndpoint;
    }

    @Value("${api.aliyun.ocr.general-endpoint:https://tysbgpu.market.alicloudapi.com/api/predict/ocr_general}")
    public void setOcrGeneralEndpoint(String ocrGeneralEndpoint) {
        if (StringUtils.isNotBlank(ocrGeneralEndpoint)) {
            this.ocrGeneralEndpoint = ocrGeneralEndpoint;
        }
    }

    public String getOfficial_appCode() {
        return official_appCode;
    }

    @Value("${api.aliyun.official.appcode:}")
    public void setOfficial_appCode(String official_appCode) {
        if (StringUtils.isNotBlank(official_appCode)) {
            this.official_appCode = official_appCode;
        } else {
            moduleUtils.disableModule(ModuleEnum.ALIYUN_API);
        }
    }
}
