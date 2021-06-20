package cn.gdeiassistant.Service.Feedback;

import org.apache.commons.io.IOUtils;
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

@Service
public class FeedbackService {

    private String senderEmail;

    private String feedbackEmail;

    private String ticketEmail;
    @Autowired
    private JavaMailSender javaMailSender;

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
     * @param username
     * @param content
     * @param inputStreams
     * @throws MessagingException
     */
    @Async
    public void SendFeedbackEmail(String username, String content, InputStream[] inputStreams) throws MessagingException, IOException {
        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true
                , StandardCharsets.UTF_8.displayName());
        mimeMessageHelper.setFrom(senderEmail);
        mimeMessageHelper.setTo(feedbackEmail);
        mimeMessageHelper.setSubject("用户" + username + "提交的意见建议反馈");
        mimeMessageHelper.setText(content);
        for (int i = 1; i <= inputStreams.length; i++) {
            mimeMessageHelper.addAttachment("attachment-image-" + i + ".jpg", new ByteArrayResource(IOUtils.toByteArray(inputStreams[i - 1])));
        }
        javaMailSender.send(mimeMailMessage);
    }

    /**
     * 发送故障工单反馈邮件
     *
     * @param username
     * @param content
     * @param type
     * @param inputStreams
     * @throws MessagingException
     */
    @Async
    public void SendTicketEmail(String username, String content, String type, InputStream[] inputStreams) throws MessagingException, IOException {
        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true
                , StandardCharsets.UTF_8.displayName());
        mimeMessageHelper.setFrom(senderEmail);
        mimeMessageHelper.setTo(ticketEmail);
        mimeMessageHelper.setSubject("用户" + username + "提交的" + type + "分类故障工单");
        mimeMessageHelper.setText(content);
        for (int i = 1; i <= inputStreams.length; i++) {
            mimeMessageHelper.addAttachment("attachment-image-" + i + ".jpg", new ByteArrayResource(IOUtils.toByteArray(inputStreams[i - 1])));
        }
        javaMailSender.send(mimeMailMessage);
    }

}
