package edu.gdei.gdeiassistant.Config.Mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

@Configuration
@PropertySource("classpath:/config/email/email.properties")
public class JavaMailConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

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
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(environment.getProperty("email.smtp.host"));
        javaMailSender.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("email.smtp.port"))));
        javaMailSender.setUsername(environment.getProperty("email.smtp.username"));
        javaMailSender.setPassword(environment.getProperty("email.smtp.password"));
        javaMailSender.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", environment.getProperty("email.smtp.auth"));
        properties.setProperty("mail.smtp.timeout", environment.getProperty("email.smtp.timeout"));
        if (Boolean.TRUE.equals(Boolean.valueOf(environment.getProperty("email.ssl.encryption")))) {
            properties.setProperty("mail.smtp.socketFactory.port", environment.getProperty("email.smtp.port"));
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }

}
