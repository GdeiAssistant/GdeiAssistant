package cn.gdeiassistant.Service.Feedback;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class FeedbackService {

    private Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private String senderEmail;

    private String feedbackEmail;

    private String ticketEmail;

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Autowired
    private UserCertificateService userCertificateService;

    @Value("#{propertiesReader['email.smtp.username']}")
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    @Value("#{propertiesReader['email.feedback']}")
    public void setFeedbackEmail(String feedbackEmail) {
        this.feedbackEmail = feedbackEmail;
    }

    @Value("#{propertiesReader['email.ticket']}")
    public void setTicketEmail(String ticketEmail) {
        this.ticketEmail = ticketEmail;
    }

    /**
     * 发送意见建议反馈邮件
     *
     * @param sessionId
     * @param content
     * @param inputStreams
     * @throws MessagingException
     */
    @Async
    public void SendFeedbackEmail(String sessionId, String content, InputStream[] inputStreams) throws MessagingException, IOException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        if (javaMailSender != null) {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true
                    , StandardCharsets.UTF_8.displayName());
            mimeMessageHelper.setFrom(senderEmail);
            mimeMessageHelper.setTo(feedbackEmail);
            mimeMessageHelper.setSubject("用户" + user.getUsername() + "提交的意见建议反馈");
            mimeMessageHelper.setText(content);
            for (int i = 1; i <= inputStreams.length; i++) {
                mimeMessageHelper.addAttachment("attachment-image-" + i + ".jpg", new ByteArrayResource(IOUtils.toByteArray(inputStreams[i - 1])));
            }
            javaMailSender.send(mimeMailMessage);
            return;
        }
        logger.error("发送至" + feedbackEmail + "的邮箱发送失败，原因为未配置邮件发送器", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
    }

    /**
     * 发送故障工单反馈邮件
     *
     * @param sessionId
     * @param content
     * @param type
     * @param inputStreams
     * @throws MessagingException
     */
    @Async
    public void SendTicketEmail(String sessionId, String content, String type, InputStream[] inputStreams) throws MessagingException, IOException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        if (javaMailSender != null) {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true
                    , StandardCharsets.UTF_8.displayName());
            mimeMessageHelper.setFrom(senderEmail);
            mimeMessageHelper.setTo(ticketEmail);
            mimeMessageHelper.setSubject("用户" + user.getUsername() + "提交的" + type + "分类故障工单");
            mimeMessageHelper.setText(content);
            for (int i = 1; i <= inputStreams.length; i++) {
                mimeMessageHelper.addAttachment("attachment-image-" + i + ".jpg", new ByteArrayResource(IOUtils.toByteArray(inputStreams[i - 1])));
            }
            javaMailSender.send(mimeMailMessage);
        }
        logger.error("发送至" + ticketEmail + "的邮箱发送失败，原因为未配置邮件发送器", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
    }

}
