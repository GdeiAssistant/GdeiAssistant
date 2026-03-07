package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.VerificationException.SendEmailException;
import cn.gdeiassistant.common.tools.SpringUtils.EmailUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.capability.email.EmailVerificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class SmtpEmailVerificationSender implements EmailVerificationSender {

    @Autowired(required = false)
    private EmailUtils emailUtils;

    private String smtpHost;
    private String senderEmail;
    private String smtpPassword;

    @Value("${email.smtp.host:}")
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    @Value("${email.smtp.username:}")
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    @Value("${email.smtp.password:}")
    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    @Override
    public void sendVerificationCode(String recipientEmail, int code) throws SendEmailException {
        String text = "您本次的邮箱验证码为：" + code + "，5分钟内有效。";
        if (emailUtils == null || !isSmtpConfigured()) {
            throw new SendEmailException("邮件功能未启用：请先配置 SMTP（email.smtp.host/username/password）");
        }
        try {
            emailUtils.SendEmail(senderEmail, recipientEmail, "广东二师助手邮箱验证码", text, new InputStream[0]);
        } catch (MessagingException | IOException e) {
            throw new SendEmailException("SMTP 邮件发送失败，请检查 SMTP 配置或服务状态");
        }
    }

    private boolean isSmtpConfigured() {
        return StringUtils.isNotBlank(smtpHost)
                && StringUtils.isNotBlank(senderEmail)
                && StringUtils.isNotBlank(smtpPassword);
    }
}
