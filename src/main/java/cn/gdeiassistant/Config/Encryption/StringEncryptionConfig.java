package cn.gdeiassistant.Config.Encryption;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Pojo.Entity.StringEncryptConfig;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Objects;

@Configuration
@PropertySource("classpath:/config/jaq/encryptor.properties")
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
    @Bean("stringEncryptConfig")
    public StringEncryptConfig developmentStringEncryptConfig() throws IOException {
        String appkey = environment.getProperty("encryptor.appkey");
        String location = environment.getProperty("encryptor.config.location");
        if (StringUtils.isNotBlank(appkey) && StringUtils.isNotBlank(location)) {
            StringEncryptConfig stringEncryptConfig = new StringEncryptConfig();
            stringEncryptConfig.setAppkey(environment.getProperty("encryptor.appkey"));
            stringEncryptConfig.setConfigLocation(new ClassPathResource(Objects.requireNonNull(environment
                    .getProperty("encryptor.config.location"))).getFile().getAbsolutePath());
            return stringEncryptConfig;
        }
        //安全加密功能模块未配置启用
        moduleUtils.DisableModule(ModuleEnum.ENCRYPTION);
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
