package edu.gdei.gdeiassistant.Config;

import edu.gdei.gdeiassistant.Pojo.Entity.StringEncryptConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:/config/jaq/encryptor.properties")
public class StringEncryptionConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    /**
     * 加载开发环境加密配置
     *
     * @return
     */
    @Bean("stringEncryptConfig")
    @Profile("development")
    public StringEncryptConfig developmentStringEncryptConfig() {
        StringEncryptConfig stringEncryptConfig = new StringEncryptConfig();
        stringEncryptConfig.setAppkey(environment.getProperty("encryptor.appkey"));
        stringEncryptConfig.setConfigLocation(environment.getProperty("encryptor.config.dev.location"));
        return stringEncryptConfig;
    }

    /**
     * 加载生产环境加密配置
     *
     * @return
     */
    @Bean("stringEncryptConfig")
    @Profile("production")
    public StringEncryptConfig productionStringEncryptConfig() {
        StringEncryptConfig stringEncryptConfig = new StringEncryptConfig();
        stringEncryptConfig.setAppkey(environment.getProperty("encryptor.appkey"));
        stringEncryptConfig.setConfigLocation(environment.getProperty("encryptor.config.pro.location"));
        return stringEncryptConfig;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
