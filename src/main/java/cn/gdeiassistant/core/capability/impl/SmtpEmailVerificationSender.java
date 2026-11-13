package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.verificationexception.SendEmailException;
import cn.gdeiassistant.common.tools.springutils.EmailUtils;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import cn.gdeiassistant.core.capability.email.EmailVerificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import cn.gdeiassistant.common.i18n.Messages;

@Component
public class SmtpEmailVerificationSender implements EmailVerificationSender {

    private final EmailUtils emailUtils;

    public SmtpEmailVerificationSender(@Autowired(required = false) EmailUtils emailUtils) {
        this.emailUtils = emailUtils;
    }

    private String smtpHost;
    private String senderEmail;
    private String smtpPassword;

    @Value("${email.smtp.host:}")
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    @Value("${email.from:${email.smtp.username:}}")
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
            throw new SendEmailException(Messages.get("capability.smtpNotConfigured"));
        }
        try {
            emailUtils.sendEmail(senderEmail, recipientEmail, "广东二师助手邮箱验证码", text, new byte[0][]);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            throw new SendEmailException(Messages.get("capability.smtpSendFailed"));
        }
    }

    private boolean isSmtpConfigured() {
        return StringUtils.isNotBlank(smtpHost)
                && StringUtils.isNotBlank(senderEmail)
                && StringUtils.isNotBlank(smtpPassword);
    }
}
