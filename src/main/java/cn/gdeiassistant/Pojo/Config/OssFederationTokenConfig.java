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

    private String ossFederationAccessKeyID;

    private String ossFederationAccessKeySecret;

    private String ossFederationRoleArn;

    private long ossTokenExpireTime;

    private String ossPolicy;

    @Autowired
    private ModuleUtils moduleUtils;

    @Value("#{propertiesReader['osstoken.aliyun.accessKeyID']}")
    public void setOssFederationAccessKeyID(String ossFederationAccessKeyID) {
        if (StringUtils.isNotBlank(ossFederationAccessKeyID)) {
            this.ossFederationAccessKeyID = ossFederationAccessKeyID;

        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_OSS);
        }
    }

    @Value("#{propertiesReader['osstoken.aliyun.accessKeySecret']}")
    public void setOssFederationAccessKeySecret(String ossFederationAccessKeySecret) {
        if (StringUtils.isNotBlank(ossFederationAccessKeySecret)) {
            this.ossFederationAccessKeySecret = ossFederationAccessKeySecret;

        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_OSS);
        }
    }

    @Value("#{propertiesReader['osstoken.aliyun.roleArn']}")
    public void setOssFederationRoleArn(String ossFederationRoleArn) {
        if (StringUtils.isNotBlank(ossFederationRoleArn)) {
            this.ossFederationRoleArn = ossFederationRoleArn;

        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_OSS);
        }
    }

    @Value("#{propertiesReader['osstoken.aliyun.tokenExpireTime']}")
    public void setOssTokenExpireTime(String ossTokenExpireTime) {
        if (StringUtils.isNotBlank(ossTokenExpireTime)) {
            this.ossTokenExpireTime = Long.parseLong(ossTokenExpireTime);
        } else {
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_OSS);
        }
    }

    @Value("#{propertiesReader['osstoken.aliyun.policyFile']}")
    public void setOssPolicy(String policyFileLocation) {
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
                ossPolicy = stringBuilder.toString();
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
            moduleUtils.DisableModule(ModuleEnum.ALIYUN_OSS);
        }
    }

    public String getOssFederationAccessKeyID() {
        return ossFederationAccessKeyID;
    }

    public String getOssFederationAccessKeySecret() {
        return ossFederationAccessKeySecret;
    }

    public String getOssFederationRoleArn() {
        return ossFederationRoleArn;
    }

    public long getOssTokenExpireTime() {
        return ossTokenExpireTime;
    }

    public String getOssPolicy() {
        return ossPolicy;
    }
}
