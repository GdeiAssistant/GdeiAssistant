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
public class AliYunSMSConfig {

    private String smsAliyunUserId;

    private String smsAliyunAccessKeyId;

    private String smsAliyunAccessKeySecret;

    private String smsAliyunChinaTemplateCode;

    private String smsAliyunGlobalTemplateCode;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['sms.aliyun.userid']}")
    public void setSmsAliyunUserId(String smsAliyunUserId) {
        if (StringUtils.isNotBlank(smsAliyunUserId)) {
            this.smsAliyunUserId = smsAliyunUserId;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_SMS);
        }
    }

    @Value("#{propertiesReader['sms.aliyun.accesskey.id']}")
    public void setSmsAliyunAccessKeyId(String smsAliyunAccessKeyId) {
        if (StringUtils.isNotBlank(smsAliyunAccessKeyId)) {
            this.smsAliyunAccessKeyId = smsAliyunAccessKeyId;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_SMS);
        }
    }

    @Value("#{propertiesReader['sms.aliyun.accesskey.secret']}")
    public void setSmsAliyunAccessKeySecret(String smsAliyunAccessKeySecret) {
        if (StringUtils.isNotBlank(smsAliyunAccessKeySecret)) {
            this.smsAliyunAccessKeySecret = smsAliyunAccessKeySecret;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_SMS);
        }
    }

    @Value("#{propertiesReader['sms.aliyun.china.templatecode']}")
    public void setSmsAliyunChinaTemplateCode(String smsAliyunChinaTemplateCode) {
        if (StringUtils.isNotBlank(smsAliyunChinaTemplateCode)) {
            this.smsAliyunChinaTemplateCode = smsAliyunChinaTemplateCode;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_SMS);
        }
    }

    @Value("#{propertiesReader['sms.aliyun.global.templatecode']}")
    public void setSmsAliyunGlobalTemplateCode(String smsAliyunGlobalTemplateCode) {
        if (StringUtils.isNotBlank(smsAliyunGlobalTemplateCode)) {
            this.smsAliyunGlobalTemplateCode = smsAliyunGlobalTemplateCode;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_SMS);
        }
    }

    public String getSmsAliyunUserId() {
        return smsAliyunUserId;
    }

    public String getSmsAliyunAccessKeyId() {
        return smsAliyunAccessKeyId;
    }

    public String getSmsAliyunAccessKeySecret() {
        return smsAliyunAccessKeySecret;
    }

    public String getSmsAliyunChinaTemplateCode() {
        return smsAliyunChinaTemplateCode;
    }

    public String getSmsAliyunGlobalTemplateCode() {
        return smsAliyunGlobalTemplateCode;
    }
}
