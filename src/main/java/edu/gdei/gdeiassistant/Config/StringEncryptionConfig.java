package edu.gdei.gdeiassistant.Config;

import edu.gdei.gdeiassistant.Pojo.Entity.StringEncryptConfig;
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

    /**
     * 加载加密配置
     *
     * @return
     */
    @Bean("stringEncryptConfig")
    public StringEncryptConfig developmentStringEncryptConfig() throws IOException {
        StringEncryptConfig stringEncryptConfig = new StringEncryptConfig();
        stringEncryptConfig.setAppkey(environment.getProperty("encryptor.appkey"));
        stringEncryptConfig.setConfigLocation(new ClassPathResource(Objects.requireNonNull(environment
                .getProperty("encryptor.config.location"))).getFile().getAbsolutePath());
        return stringEncryptConfig;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
