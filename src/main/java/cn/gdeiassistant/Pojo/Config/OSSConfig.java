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
public class OSSConfig {

    private String accessKeyID;

    private String accessKeySecret;

    private String endpoint;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['oss.accessKeySecret']}")
    public void setAccessKeySecret(String accessKeySecret) {
        if(StringUtils.isNotBlank(accessKeySecret)){
            this.accessKeySecret = accessKeySecret;
        }
        else{
            moduleUtils.DisableModule(ModuleEnum.OSS);
        }
    }

    @Value("#{propertiesReader['oss.accessKeyID']}")
    public void setAccessKeyID(String accessKeyID) {
        if(StringUtils.isNotBlank(accessKeyID)){
            this.accessKeyID = accessKeyID;
        }
        else{
            moduleUtils.DisableModule(ModuleEnum.OSS);
        }
    }

    @Value("#{propertiesReader['oss.endpoint']}")
    public void setEndpoint(String endpoint) {
        if(StringUtils.isNotBlank(endpoint)){
            this.endpoint = endpoint;
        }
        else{
            moduleUtils.DisableModule(ModuleEnum.OSS);
        }
    }

    public String getAccessKeyID() {
        return accessKeyID;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
