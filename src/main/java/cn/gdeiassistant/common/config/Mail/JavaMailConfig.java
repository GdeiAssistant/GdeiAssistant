package cn.gdeiassistant.common.config.Mail;

import cn.gdeiassistant.common.enums.Module.ModuleEnum;
import cn.gdeiassistant.common.tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
public class JavaMailConfig {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

    @PostConstruct
    public void disableEmailModuleWhenSmtpMissing() {
        if (!isMailSenderConfigured()) {
            moduleUtils.DisableModule(ModuleEnum.EMAIL);
        }
    }

    /**
     * 邮件发送器
     *
     * @return
     */
    @Bean
    @ConditionalOnExpression(
            "#{T(cn.gdeiassistant.common.tools.Utils.StringUtils).isNotBlank('${email.smtp.host:}') " +
                    "and T(cn.gdeiassistant.common.tools.Utils.StringUtils).isNumeric('${email.smtp.port:465}') " +
                    "and T(cn.gdeiassistant.common.tools.Utils.StringUtils).isNotBlank('${email.smtp.username:}') " +
                    "and T(cn.gdeiassistant.common.tools.Utils.StringUtils).isNotBlank('${email.smtp.password:}')}"
    )
    public JavaMailSenderImpl javaMailSender() {
        String host = environment.getProperty("email.smtp.host");
        String port = environment.getProperty("email.smtp.port");
        String username = environment.getProperty("email.smtp.username");
        String password = environment.getProperty("email.smtp.password");
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPort(resolveSmtpPort(port));
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

    private boolean isMailSenderConfigured() {
        String host = environment.getProperty("email.smtp.host");
        String port = environment.getProperty("email.smtp.port");
        String username = environment.getProperty("email.smtp.username");
        String password = environment.getProperty("email.smtp.password");
        return StringUtils.isNotBlank(host)
                && StringUtils.isNotBlank(port)
                && StringUtils.isNumeric(port)
                && StringUtils.isNotBlank(username)
                && StringUtils.isNotBlank(password);
    }

    private int resolveSmtpPort(String port) {
        if (StringUtils.isBlank(port)) {
            return 465;
        }
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException ignored) {
            return 465;
        }
    }

}
