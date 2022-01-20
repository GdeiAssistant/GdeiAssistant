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
public class JisuConfig {

    @Autowired
    private ModuleUtils moduleUtils;

    private String host;

    private String recognitionPath;

    private String ipaddressPath;

    private String appkey;

    @Value("#{propertiesReader['api.jisu.host']}")
    public void setHost(String host) {
        if (StringUtils.isNotBlank(host)) {
            this.host = host;
        } else {
            moduleUtils.DisableModule(ModuleEnum.JISU);
        }
    }

    @Value("#{propertiesReader['api.jisu.ipadress.path']}")
    public void setIpaddressPath(String ipaddressPath) {
        if (StringUtils.isNotBlank(ipaddressPath)) {
            this.ipaddressPath = ipaddressPath;
        } else {
            moduleUtils.DisableModule(ModuleEnum.JISU);
        }
    }

    @Value("#{propertiesReader['api.jisu.recognize.path']}")
    public void setRecognitionPath(String recognitionPath) {
        if (StringUtils.isNotBlank(recognitionPath)) {
            this.recognitionPath = recognitionPath;
        } else {
            moduleUtils.DisableModule(ModuleEnum.JISU);
        }
    }

    @Value("#{propertiesReader['api.jisu.appkey']}")
    public void setAppkey(String appkey) {
        if (StringUtils.isNotBlank(appkey)) {
            this.appkey = appkey;
        } else {
            moduleUtils.DisableModule(ModuleEnum.JISU);
        }
    }

    public String getHost() {
        return host;
    }

    public String getRecognitionPath() {
        return recognitionPath;
    }

    public String getIpaddressPath() {
        return ipaddressPath;
    }

    public String getAppkey() {
        return appkey;
    }
}
