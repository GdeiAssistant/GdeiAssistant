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
public class AliyunConfig {

    private String aliyunUserId;

    private String aliyunAccessKeyId;

    private String aliyunAccessKeySecret;

    private String aliyun_h5_verifyKey;

    private String official_appCode;

    private String aliyunSMSChinaTemplateCode;

    private String aliyunSMSGlobalTemplateCode;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['api.aliyun.userid']}")
    public void setAliyunUserId(String aliyunUserId) {
        if (StringUtils.isNotBlank(aliyunUserId)) {
            this.aliyunUserId = aliyunUserId;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN);
        }
    }

    @Value("#{propertiesReader['api.aliyun.accesskey.id']}")
    public void setAliyunAccessKeyId(String aliyunAccessKeyId) {
        if (StringUtils.isNotBlank(aliyunAccessKeyId)) {
            this.aliyunAccessKeyId = aliyunAccessKeyId;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN);
        }
    }

    @Value("#{propertiesReader['api.aliyun.accesskey.secret']}")
    public void setAliyunAccessKeySecret(String aliyunAccessKeySecret) {
        if (StringUtils.isNotBlank(aliyunAccessKeySecret)) {
            this.aliyunAccessKeySecret = aliyunAccessKeySecret;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN);
        }
    }

    @Value("#{propertiesReader['api.aliyun.h5.verifykey']}")
    public void setAliyun_h5_verifyKey(String aliyun_h5_verifyKey) {
        if (StringUtils.isNotBlank(aliyun_h5_verifyKey)) {
            this.aliyun_h5_verifyKey = aliyun_h5_verifyKey;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN);
        }
    }

    @Value("#{propertiesReader['api.aliyun.official.appcode']}")
    public void setOfficial_appCode(String official_appCode) {
        if (StringUtils.isNotBlank(official_appCode)) {
            this.official_appCode = official_appCode;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN);
        }
    }

    @Value("#{propertiesReader['api.aliyun.sms.china.templatecode']}")
    public void setAliyunSMSChinaTemplateCode(String aliyunSMSChinaTemplateCode) {
        if (StringUtils.isNotBlank(aliyunSMSChinaTemplateCode)) {
            this.aliyunSMSChinaTemplateCode = aliyunSMSChinaTemplateCode;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN);
        }
    }

    @Value("#{propertiesReader['api.aliyun.sms.global.templatecode']}")
    public void setAliyunSMSGlobalTemplateCode(String aliyunSMSGlobalTemplateCode) {
        if (StringUtils.isNotBlank(aliyunSMSGlobalTemplateCode)) {
            this.aliyunSMSGlobalTemplateCode = aliyunSMSGlobalTemplateCode;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN);
        }
    }

    public String getAliyunUserId() {
        return aliyunUserId;
    }

    public String getAliyunAccessKeyId() {
        return aliyunAccessKeyId;
    }

    public String getAliyunAccessKeySecret() {
        return aliyunAccessKeySecret;
    }

    public String getAliyun_h5_verifyKey() {
        return aliyun_h5_verifyKey;
    }

    public String getOfficial_appCode() {
        return official_appCode;
    }

    public String getAliyunSMSChinaTemplateCode() {
        return aliyunSMSChinaTemplateCode;
    }

    public String getAliyunSMSGlobalTemplateCode() {
        return aliyunSMSGlobalTemplateCode;
    }
}
