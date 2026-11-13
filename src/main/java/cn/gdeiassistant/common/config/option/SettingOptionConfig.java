package cn.gdeiassistant.common.config.option;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;
import java.util.Objects;

@Configuration
public class SettingOptionConfig {

    private final Environment environment;

    private final ServletContext servletContext;

    public SettingOptionConfig(Environment environment, ServletContext servletContext) {
        this.environment = environment;
        this.servletContext = servletContext;
    }

    /**
     * 检查是否使用黑白网页（theme.grayscale 来自 application.yml，缺省为 0）
     */
    @PostConstruct
    public void checkUsingGrayScaleTheme() {
        String value = environment.getProperty("theme.grayscale", "0");
        if ("1".equals(value.trim())) {
            servletContext.setAttribute("grayscale", true);
        }
    }
}
