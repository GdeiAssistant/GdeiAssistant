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
public class AliYunOSSConfig {

    private String ossAccessKeyID;

    private String ossAccessKeySecret;

    private String ossEndpoint;

    private String ossRegion;

    private String ossVersion;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['oss.aliyun.accessKeySecret']}")
    public void setOssAccessKeySecret(String ossAccessKeySecret) {
        if (StringUtils.isNotBlank(ossAccessKeySecret)) {
            this.ossAccessKeySecret = ossAccessKeySecret;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_OSS);
        }
    }

    @Value("#{propertiesReader['oss.aliyun.accessKeyID']}")
    public void setOssAccessKeyID(String ossAccessKeyID) {
        if (StringUtils.isNotBlank(ossAccessKeyID)) {
            this.ossAccessKeyID = ossAccessKeyID;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_OSS);
        }
    }

    @Value("#{propertiesReader['oss.aliyun.endpoint']}")
    public void setOssEndpoint(String ossEndpoint) {
        if (StringUtils.isNotBlank(ossEndpoint)) {
            this.ossEndpoint = ossEndpoint;
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_OSS);
        }
    }

    @Value("#{propertiesReader['oss.aliyun.region']}")
    public void setOssRegion(String ossRegion) {
        this.ossRegion = ossRegion;
    }

    @Value("#{propertiesReader['oss.aliyun.version']}")
    public void setVersion(String version) {
        this.ossVersion = version;
    }


    public String getOssAccessKeyID() {
        return ossAccessKeyID;
    }

    public String getOssAccessKeySecret() {
        return ossAccessKeySecret;
    }

    public String getOssEndpoint() {
        return ossEndpoint;
    }

    public String getOssRegion() {
        return ossRegion;
    }

    public String getVersion() {
        return ossVersion;
    }
}
