package edu.gdei.gdeiassistant.Config.Option;

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
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("authentication.force"))).equals(1)) {
            servletContext.setAttribute("authentication.force", true);
        }
    }

    /**
     * 检查是否使用黑白网页
     */
    @Bean
    public void CheckUsingGrayScaleTheme() {
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("theme.grayscale"))).equals(1)) {
            servletContext.setAttribute("grayscale", true);
        }
    }

    /**
     * 检查Logo主题
     */
    @Bean
    public void CheckLogoTheme() {
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("theme.logo.pink"))).equals(1)) {
            servletContext.setAttribute("pinkthemelogo", true);
        }
        else if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("theme.logo.pride"))).equals(1)) {
            servletContext.setAttribute("pridethemelogo", true);
        }

    }

    /**
     * 检查各功能模块是否启用实名认证
     */
    @Bean
    public void CheckFunctionalAuthentication() {
        //二手交易
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("authentication.ershou"))).equals(1)) {
            servletContext.setAttribute("authentication.ershou", true);
        }
        //失物招领
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("authentication.lostandfound"))).equals(1)) {
            servletContext.setAttribute("authentication.lostandfound", true);
        }
        //校园树洞
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("authentication.secret"))).equals(1)) {
            servletContext.setAttribute("authentication.secret", true);
        }
        //全民快递
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("authentication.delivery"))).equals(1)) {
            servletContext.setAttribute("authentication.delivery", true);
        }
        //拍好校园
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("authentication.photograph"))).equals(1)) {
            servletContext.setAttribute("authentication.photograph", true);
        }
        //表白墙
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("authentication.express"))).equals(1)) {
            servletContext.setAttribute("authentication.express", true);
        }
        //话题
        if (Integer.valueOf(Objects.requireNonNull(environment.getProperty("authentication.topic"))).equals(1)) {
            servletContext.setAttribute("authentication.topic", true);
        }
    }
}
