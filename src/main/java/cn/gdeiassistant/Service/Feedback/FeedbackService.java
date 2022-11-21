package cn.gdeiassistant.Service.Feedback;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FeedbackService {

    private String senderEmail;

    private String feedbackEmail;

    private String ticketEmail;

    @Autowired
    private EmailUtils emailUtils;

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
        emailUtils.SendEmail(senderEmail, feedbackEmail, "用户" + user.getUsername() + "提交的意见建议反馈"
                , content, inputStreams);
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
        emailUtils.SendEmail(senderEmail, ticketEmail, "用户" + user.getUsername() + "提交的"
                + type + "分类故障工单", content, inputStreams);
    }

}
