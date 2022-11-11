package cn.gdeiassistant.Config.Encryption;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Pojo.Encryption.AESEncryptConfig;
import cn.gdeiassistant.Pojo.Encryption.EncryptConfig;
import cn.gdeiassistant.Pojo.Encryption.JAQEncryptConfig;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
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
@PropertySources({@PropertySource("classpath:/config/encrypt/encrypt.properties")})
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
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
