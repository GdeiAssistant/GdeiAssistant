package cn.gdeiassistant.common.config.encryption;

import cn.gdeiassistant.common.enums.module.ModuleEnum;
import cn.gdeiassistant.common.pojo.encryption.AESEncryptConfig;
import cn.gdeiassistant.common.pojo.encryption.EncryptConfig;
import cn.gdeiassistant.common.tools.springutils.ModuleUtils;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Configuration
public class StringEncryptionConfig {

    private final Environment environment;

    private final ModuleUtils moduleUtils;

    public StringEncryptionConfig(Environment environment, ModuleUtils moduleUtils) {
        this.environment = environment;
        this.moduleUtils = moduleUtils;
    }

    /**
     * 加载加密配置
     *
     * @return
     */
    @Bean("encryptConfig")
    public EncryptConfig encryptConfig() throws IOException {
        if (Boolean.parseBoolean(environment.getProperty("encrypt.enable"))
                && "aes".equals(environment.getProperty("encrypt.type"))) {
            String privateKey = environment.getProperty("encrypt.private.key");
            if (StringUtils.isNotBlank(privateKey)) {
                AESEncryptConfig aesEncryptionConfig = new AESEncryptConfig();
                aesEncryptionConfig.setPrivateKey(privateKey);
                return aesEncryptionConfig;
            }
            moduleUtils.disableModule(ModuleEnum.ENCRYPTION);
        }
        //安全加密功能模块未启用
        //生产环境下拒绝启动，防止敏感字段静默落成明文
        for (String profile : environment.getActiveProfiles()) {
            if ("production".equalsIgnoreCase(profile) || "staging".equalsIgnoreCase(profile)) {
                throw new IllegalStateException(
                        "Field encryption must be enabled in staging/production. " +
                        "Set ENCRYPT_ENABLE=true and ENCRYPT_PRIVATE_KEY in environment.");
            }
        }
        return null;
    }
}
