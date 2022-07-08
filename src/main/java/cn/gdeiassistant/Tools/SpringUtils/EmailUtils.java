package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Service.AccountManagement.Feedback.FeedbackService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class EmailUtils {

    private Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    /**
     * 发送电子邮件
     *
     * @param sender
     * @param recipient
     * @param subject
     * @param text
     * @param inputStreams
     * @throws MessagingException
     * @throws IOException
     */
    public void SendEmail(String sender, String recipient, String subject, String text
            , InputStream[] inputStreams) throws MessagingException, IOException {
        if (javaMailSender != null) {
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true
                    , StandardCharsets.UTF_8.displayName());
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text);
            for (int i = 1; i <= inputStreams.length; i++) {
                mimeMessageHelper.addAttachment("attachment-image-" + i + ".jpg"
                        , new ByteArrayResource(IOUtils.toByteArray(inputStreams[i - 1])));
            }
            javaMailSender.send(mimeMailMessage);
            return;
        }
        logger.error("发送至" + recipient + "的邮箱发送失败，原因为未配置邮件发送器", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
    }
}
