package edu.gdei.gdeiassistant.Config;

import edu.gdei.gdeiassistant.Pojo.Entity.YiBanAuthorizeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource({"classpath:/config/yiban/dating.properties", "classpath:/config/yiban/ershou.properties"
        , "classpath:/config/yiban/lostandfound.properties", "classpath:/config/yiban/news.properties"
        , "classpath:/config/yiban/secret.properties", "classpath:/config/yiban/userlogin.properties"})
public class YiBanAppAuthorizationConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    /**
     * 易班网站接入授权信息
     *
     * @return
     */
    @Bean
    public Map<String, YiBanAuthorizeInfo> yiBanAuthorizeMap() {
        Map<String, YiBanAuthorizeInfo> map = new HashMap<>();
        return map;
    }

    /**
     * 易班站内应用授权信息
     *
     * @return
     */
    @Bean
    public Map<String, YiBanAuthorizeInfo> yiBanAppAuthorizeMap() {
        Map<String, YiBanAuthorizeInfo> map = new HashMap<>();
        return map;
    }

    /**
     * 易班轻应用授权信息
     *
     * @return
     */
    @Bean
    public Map<String, YiBanAuthorizeInfo> yiBanLightAppAuthorizeMap() {
        Map<String, YiBanAuthorizeInfo> map = new HashMap<>();
        map.put("userlogin", new YiBanAuthorizeInfo(environment.getProperty("yiban.userlogin.appId"), environment.getProperty("yiban.userlogin.appSecret")
                , environment.getProperty("yiban.userlogin.callbackURL")));
        map.put("secret", new YiBanAuthorizeInfo(environment.getProperty("yiban.secret.appId"), environment.getProperty("yiban.secret.appSecret")
                , environment.getProperty("yiban.secret.callbackURL")));
        map.put("ershou", new YiBanAuthorizeInfo(environment.getProperty("yiban.ershou.appId"), environment.getProperty("yiban.ershou.appSecret")
                , environment.getProperty("yiban.ershou.callbackURL")));
        map.put("lostandfound", new YiBanAuthorizeInfo(environment.getProperty("yiban.lostandfound.appId"), environment.getProperty("yiban.lostandfound.appSecret")
                , environment.getProperty("yiban.lostandfound.callbackURL")));
        map.put("dating", new YiBanAuthorizeInfo(environment.getProperty("yiban.dating.appId"), environment.getProperty("yiban.dating.appSecret")
                , environment.getProperty("yiban.dating.callbackURL")));
        map.put("news", new YiBanAuthorizeInfo(environment.getProperty("yiban.news.appId"), environment.getProperty("yiban.news.appSecret")
                , environment.getProperty("yiban.news.callbackURL")));
        return map;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
