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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FeedbackService {

    private String senderEmail;

    private String feedbackEmail;

    private String ticketEmail;

    @Autowired(required = false)
    private EmailUtils emailUtils;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Value("${email.smtp.username:}")
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    @Value("${email.feedback:}")
    public void setFeedbackEmail(String feedbackEmail) {
        this.feedbackEmail = feedbackEmail;
    }

    @Value("${email.ticket:}")
    public void setTicketEmail(String ticketEmail) {
        this.ticketEmail = ticketEmail;
    }

    private boolean isEmailEnabled() {
        return emailUtils != null && StringUtils.isNotBlank(senderEmail)
                && StringUtils.isNotBlank(feedbackEmail) && StringUtils.isNotBlank(ticketEmail);
    }

    /**
     * 发送意见建议反馈邮件
     */
    @Async
    public void sendFeedbackEmail(String sessionId, String content, InputStream[] inputStreams) throws MessagingException, IOException {
        if (!isEmailEnabled()) {
            throw new FeatureNotEnabledException("邮件服务未启用，无法提交工单/反馈");
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        emailUtils.sendEmail(senderEmail, feedbackEmail, "用户" + user.getUsername() + "提交的意见建议反馈"
                , content, inputStreams);
    }

    /**
     * 发送故障工单反馈邮件
     */
    @Async
    public void sendTicketEmail(String sessionId, String content, String type, InputStream[] inputStreams) throws MessagingException, IOException {
        if (!isEmailEnabled()) {
            throw new FeatureNotEnabledException("邮件服务未启用，无法提交工单/反馈");
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        emailUtils.sendEmail(senderEmail, ticketEmail, "用户" + user.getUsername() + "提交的"
                + type + "分类故障工单", content, inputStreams);
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
