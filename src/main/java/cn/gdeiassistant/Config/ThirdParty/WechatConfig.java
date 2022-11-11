package cn.gdeiassistant.Config.ThirdParty;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Pojo.Config.WechatMiniProgramConfig;
import cn.gdeiassistant.Pojo.Config.WechatOfficialAccountConfig;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Configuration
@PropertySources({@PropertySource("classpath:/config/wechat/account.properties"),@PropertySource("classpath:/config/wechat/app.properties")})
public class WechatConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 开发环境微信小程序配置
     *
     * @return
     */
    @Bean(name = "wechatMiniProgramConfig")
    @Profile("development")
    @Qualifier("developmentWechatMiniProgramConfig")
    public WechatMiniProgramConfig developmentWechatMiniProgramConfig() {
        WechatMiniProgramConfig config = new WechatMiniProgramConfig();
        String appid = null;
        String appsecret = null;
        appid = environment.getProperty("wechat.dev.app.appid");
        appsecret = environment.getProperty("wechat.dev.app.secret");
        if (StringUtils.isNotBlank(appid) && StringUtils.isNotBlank(appsecret)) {
            config.setAppid(appid);
            config.setAppsecret(appsecret);
        } else {
            moduleUtils.DisableModule(ModuleEnum.WECHAT_MINI_PROGRAM);
        }
        return config;
    }

    /**
     * 生产环境微信小程序配置
     *
     * @return
     */
    @Bean(name = "wechatMiniProgramConfig")
    @Profile("production")
    @Qualifier("productionWechatMiniProgramConfig")
    public WechatMiniProgramConfig productionWechatMiniProgramConfig() {
        WechatMiniProgramConfig config = new WechatMiniProgramConfig();
        String appid = null;
        String appsecret = null;
        appid = environment.getProperty("wechat.pro.app.appid");
        appsecret = environment.getProperty("wechat.pro.app.secret");
        if (StringUtils.isNotBlank(appid) && StringUtils.isNotBlank(appsecret)) {
            config.setAppid(appid);
            config.setAppsecret(appsecret);
        } else {
            moduleUtils.DisableModule(ModuleEnum.WECHAT_MINI_PROGRAM);
        }
        return config;
    }

    /**
     * 开发环境微信公众号配置
     *
     * @return
     */
    @Bean(name = "wechatOfficialAccountConfig")
    @Profile("development")
    @Qualifier("developmentwechatOfficialAccountConfig")
    public WechatOfficialAccountConfig developmentWechatOfficialAccountConfig() {
        WechatOfficialAccountConfig config = new WechatOfficialAccountConfig();
        String appid = null;
        String appsecret = null;
        String token = null;
        appid = environment.getProperty("wechat.dev.account.appid");
        appsecret = environment.getProperty("wechat.dev.account.appsecret");
        token = environment.getProperty("wechat.dev.account.token");
        if (StringUtils.isNotBlank(appid) && StringUtils.isNotBlank(appsecret)
                && StringUtils.isNotBlank(token)) {
            config.setAppid(appid);
            config.setAppsecret(appsecret);
            config.setToken(token);
        } else {
            moduleUtils.DisableModule(ModuleEnum.WECHAT_OFFICIAL_ACCOUNT);
        }
        return config;
    }

    /**
     * 生产环境微信公众号配置
     *
     * @return
     */
    @Bean(name = "wechatOfficialAccountConfig")
    @Profile("production")
    @Qualifier("productionWechatOfficialAccountConfig")
    public WechatOfficialAccountConfig productionWechatOfficialAccountConfig() {
        WechatOfficialAccountConfig config = new WechatOfficialAccountConfig();
        String appid = null;
        String appsecret = null;
        String token = null;
        appid = environment.getProperty("wechat.pro.account.appid");
        appsecret = environment.getProperty("wechat.pro.account.appsecret");
        token = environment.getProperty("wechat.pro.account.token");
        if (StringUtils.isNotBlank(appid) && StringUtils.isNotBlank(appsecret)
                && StringUtils.isNotBlank(token)) {
            config.setAppid(appid);
            config.setAppsecret(appsecret);
            config.setToken(token);
        } else {
            moduleUtils.DisableModule(ModuleEnum.WECHAT_OFFICIAL_ACCOUNT);
        }
        return config;
    }
}
