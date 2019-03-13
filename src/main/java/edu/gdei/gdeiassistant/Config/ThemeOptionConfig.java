package edu.gdei.gdeiassistant.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;
import java.util.Objects;

@Configuration
@PropertySource("classpath:/config/theme/theme.properties")
public class ThemeOptionConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ServletContext servletContext;

    /**
     * 检查是否使用黑白网页
     */
    @Bean
    public void CheckUsingGrayScaleTheme() {
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("theme.grayscale"))).equals(1)) {
            servletContext.setAttribute("grayscale", true);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
