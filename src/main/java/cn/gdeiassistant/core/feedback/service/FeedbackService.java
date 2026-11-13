package cn.gdeiassistant.core.feedback.service;

import cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import cn.gdeiassistant.core.feedback.mapper.FeedbackMapper;
import cn.gdeiassistant.core.feedback.pojo.dto.FeedbackSubmitDTO;
import cn.gdeiassistant.core.feedback.pojo.entity.FeedbackEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.springutils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import cn.gdeiassistant.common.exception.verificationexception.SendEmailException;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.gdeiassistant.common.i18n.Messages;

@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private final String senderEmail;

    private final String feedbackEmail;

    private final String ticketEmail;

    private final EmailUtils emailUtils;

    private final UserCertificateService userCertificateService;

    private final FeedbackMapper feedbackMapper;

    public FeedbackService(@Autowired(required = false) EmailUtils emailUtils,
                           UserCertificateService userCertificateService,
                           FeedbackMapper feedbackMapper,
                           @Value("${email.from:${email.smtp.username:}}") String senderEmail,
                           @Value("${email.feedback:}") String feedbackEmail,
                           @Value("${email.ticket:}") String ticketEmail) {
        this.emailUtils = emailUtils;
        this.userCertificateService = userCertificateService;
        this.feedbackMapper = feedbackMapper;
        this.senderEmail = senderEmail;
        this.feedbackEmail = feedbackEmail;
        this.ticketEmail = ticketEmail;
    }

    private boolean isEmailEnabled() {
        return emailUtils != null && StringUtils.isNotBlank(senderEmail)
                && StringUtils.isNotBlank(feedbackEmail) && StringUtils.isNotBlank(ticketEmail);
    }

    /** SMTP acceptance is required before the HTTP endpoint reports success. */
    public void sendFeedbackEmail(String sessionId, String content, byte[][] attachments) throws SendEmailException {
        sendEmail(sessionId, content, feedbackEmail, "提交的意见建议反馈", attachments);
    }

    public void sendTicketEmail(String sessionId, String content, String type, byte[][] attachments) throws SendEmailException {
        sendEmail(sessionId, content, ticketEmail, "提交的" + type + "分类故障工单", attachments);
    }

    private void sendEmail(String sessionId, String content, String recipient, String subject,
                           byte[][] attachments) throws SendEmailException {
        if (!isEmailEnabled()) {
            throw new FeatureNotEnabledException(Messages.get("feedback.mailDisabled"));
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        try {
            emailUtils.sendEmail(senderEmail, recipient, "用户" + user.getUsername() + subject, content, attachments);
        } catch (MessagingException | MailException e) {
            logger.warn("FeedbackMailFailure - exceptionType={} causeType={}", e.getClass().getSimpleName(),
                    e.getCause() != null ? e.getCause().getClass().getSimpleName() : "-");
            // No automatic resend: SMTP errors can have an unknown remote delivery outcome.
            throw new SendEmailException(Messages.get("feedback.mailSubmitFailed"));
        }
    }

    /**
     * 提交反馈并写入 MySQL（帮助与反馈 - 统一入口）
     */
    public void submitFeedback(String sessionId, FeedbackSubmitDTO dto) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        FeedbackEntity entity = new FeedbackEntity();
        entity.setUsername(user.getUsername());
        entity.setContent(dto.getContent() != null ? dto.getContent().trim() : null);
        entity.setContact(dto.getContact() != null ? dto.getContact().trim() : null);
        entity.setType(dto.getType() != null ? dto.getType().trim() : null);
        feedbackMapper.insertFeedback(entity);
    }

}
