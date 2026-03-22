package cn.gdeiassistant.common.config.Encryption;

import cn.gdeiassistant.common.enums.Module.ModuleEnum;
import cn.gdeiassistant.common.pojo.Encryption.AESEncryptConfig;
import cn.gdeiassistant.common.pojo.Encryption.EncryptConfig;
import cn.gdeiassistant.common.pojo.Encryption.JAQEncryptConfig;
import cn.gdeiassistant.common.tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Objects;

@Configuration
public class StringEncryptionConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

    /**
     * 加载加密配置
     *
     * @return
     */
    @Bean("encryptConfig")
    public EncryptConfig encryptConfig() throws IOException {
        String enable = environment.getProperty("encrypt.enable");
        if (StringUtils.isNotBlank(enable) && StringUtils.isBoolean(enable)
                && "TRUE".equalsIgnoreCase(enable)) {
            //启用加密功能
            String type = environment.getProperty("encrypt.type");
            if (StringUtils.isNotBlank(type)) {
                switch (type) {
                    case "aes":
                        //使用AES对称加密
                        String privateKey = environment.getProperty("encrypt.private.key");
                        if (StringUtils.isNotBlank(privateKey)) {
                            AESEncryptConfig aesEncryptionConfig = new AESEncryptConfig();
                            aesEncryptionConfig.setPrivateKey(privateKey);
                            return aesEncryptionConfig;
                        }
                        //安全加密功能模块未配置
                        moduleUtils.DisableModule(ModuleEnum.ENCRYPTION);
                        break;

                    case "jaq":
                        //使用阿里聚安全加密
                        String appKey = environment.getProperty("encrypt.app.key");
                        String location = environment.getProperty("encrypt.config.location");
                        if (StringUtils.isNotBlank(appKey) && StringUtils.isNotBlank(location)) {
                            JAQEncryptConfig encryptionConfig = new JAQEncryptConfig();
                            encryptionConfig.setAppKey(environment.getProperty("encrypt.app.key"));
                            encryptionConfig.setConfigLocation(new ClassPathResource(Objects.requireNonNull(environment
                                    .getProperty("encrypt.config.location"))).getFile().getAbsolutePath());
                            return encryptionConfig;
                        }
                        //安全加密功能模块未配置
                        moduleUtils.DisableModule(ModuleEnum.ENCRYPTION);
                        break;
                }
            }
        }
        //安全加密功能模块未启用
        //生产环境下拒绝启动，防止敏感字段静默落成明文
        for (String profile : environment.getActiveProfiles()) {
            if ("production".equalsIgnoreCase(profile) || "prod".equalsIgnoreCase(profile)) {
                throw new IllegalStateException(
                        "Field encryption must be enabled in production. " +
                        "Set ENCRYPT_ENABLE=true and ENCRYPT_PRIVATE_KEY in environment.");
            }
        }
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
