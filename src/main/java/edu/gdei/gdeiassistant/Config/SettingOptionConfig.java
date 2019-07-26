package edu.gdei.gdeiassistant.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;
import java.util.Objects;

@Configuration
@PropertySource("classpath:/config/setting/setting.properties")
public class SettingOptionConfig {

    @Autowired
    private Environment environment;

    @Autowired
    private ServletContext servletContext;

    /**
     * 检查是否启用强制实名认证
     */
    @Bean
    public void CheckForceAuthentication() {
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("setting.authentication.force"))).equals(1)) {
            servletContext.setAttribute("authentication.force", true);
        }
    }
}
