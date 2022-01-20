package cn.gdeiassistant.Pojo.Config;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
@Scope("singleton")
public class OssFederationTokenConfig {

    private String accessKeyID;

    private String accessKeySecret;

    private String roleArn;

    private long tokenExpireTime;

    private String policy;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['token.accessKeyID']}")
    public void setAccessKeyID(String accessKeyID) {
        if (StringUtils.isNotBlank(accessKeyID)) {
            this.accessKeyID = accessKeyID;

        } else {
            moduleUtils.DisableModule(ModuleEnum.OSS_FEDERATION);
        }
    }

    @Value("#{propertiesReader['token.accessKeySecret']}")
    public void setAccessKeySecret(String accessKeySecret) {
        if (StringUtils.isNotBlank(accessKeySecret)) {
            this.accessKeySecret = accessKeySecret;

        } else {
            moduleUtils.DisableModule(ModuleEnum.OSS_FEDERATION);
        }
    }

    @Value("#{propertiesReader['token.roleArn']}")
    public void setRoleArn(String roleArn) {
        if (StringUtils.isNotBlank(roleArn)) {
            this.roleArn = roleArn;

        } else {
            moduleUtils.DisableModule(ModuleEnum.OSS_FEDERATION);
        }
    }

    @Value("#{propertiesReader['token.tokenExpireTime']}")
    public void setTokenExpireTime(String tokenExpireTime) {
        if (StringUtils.isNotBlank(tokenExpireTime)) {
            this.tokenExpireTime = Long.parseLong(tokenExpireTime);
        } else {
            moduleUtils.DisableModule(ModuleEnum.OSS_FEDERATION);
        }
    }

    @Value("#{propertiesReader['token.policyFile']}")
    public void setPolicy(String policyFileLocation) {
        if (StringUtils.isNotBlank(policyFileLocation)) {
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new ClassPathResource(policyFileLocation).getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String temp;
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp);
                }
                policy = stringBuilder.toString();
            } catch (IOException e) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } else {
            moduleUtils.DisableModule(ModuleEnum.OSS_FEDERATION);
        }
    }

    public String getAccessKeyID() {
        return accessKeyID;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public long getTokenExpireTime() {
        return tokenExpireTime;
    }

    public String getPolicy() {
        return policy;
    }
}
