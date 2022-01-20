package cn.gdeiassistant.Config.Mail;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@PropertySource("classpath:/config/email/email.properties")
public class JavaMailConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 邮件发送器
     *
     * @return
     */
    @Bean
    public JavaMailSenderImpl javaMailSender() {
        String host = environment.getProperty("email.smtp.host");
        String port = environment.getProperty("email.smtp.port");
        String username = environment.getProperty("email.smtp.username");
        String password = environment.getProperty("email.smtp.password");
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(port)
                && StringUtils.isNotBlank(username)
                && StringUtils.isNotBlank(password)
                && StringUtils.isNumeric(port)) {
            //邮件发送器配置信息完整，设置并生成邮件发送器
            JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setHost(host);
            javaMailSender.setPort(Integer.parseInt(port));
            javaMailSender.setUsername(username);
            javaMailSender.setPassword(password);
            javaMailSender.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
            Properties properties = new Properties();
            properties.setProperty("mail.smtp.auth",
                    Boolean.TRUE.equals(Boolean.valueOf(environment.getProperty("email.smtp.auth")))
                            ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
            properties.setProperty("mail.smtp.timeout",
                    StringUtil.isBlank(environment.getProperty("email.smtp.timeout"))
                            ? "5000" : environment.getProperty("email.smtp.timeout"));
            if (Boolean.TRUE.equals(Boolean.valueOf(environment.getProperty("email.ssl.encryption")))) {
                properties.setProperty("mail.smtp.socketFactory.port", environment.getProperty("email.smtp.port"));
                properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            javaMailSender.setJavaMailProperties(properties);
            return javaMailSender;
        }
        //邮件发送器配置失败，功能模块未启用
        moduleUtils.DisableModule(ModuleEnum.EMAIL);
        return null;
    }

}
