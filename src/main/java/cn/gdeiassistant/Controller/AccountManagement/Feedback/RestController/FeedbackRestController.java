package cn.gdeiassistant.Controller.AccountManagement.Feedback.RestController;

import cn.gdeiassistant.Pojo.Entity.ClassifiedFeedback;
import cn.gdeiassistant.Pojo.Entity.Feedback;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AccountManagement.Feedback.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FeedbackRestController {

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 提交意见建议反馈表单
     *
     * @param request
     * @param feedback
     * @return
     */
    @RequestMapping(value = "/api/feedback/function", method = RequestMethod.POST)
    public JsonResult PostFunctionalFeedback(HttpServletRequest request, @Validated Feedback feedback, MultipartFile[] images) throws IOException, MessagingException {
        List<InputStream> inputStreamList = new ArrayList<>();
        if (images != null) {
            if (images.length > 9) {
                return new JsonResult(false, "不合法的图片文件");
            }
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty() || file.getSize() >= MAX_PICTURE_SIZE) {
                    return new JsonResult(false, "不合法的图片文件");
                }
                InputStream inputStream = file.getInputStream();
                inputStreamList.add(inputStream);
            }
        }
        feedbackService.SendFeedbackEmail(request.getSession().getId(), feedback.getContent(), inputStreamList.toArray(new InputStream[0]));
        return new JsonResult(true);
    }

    /**
     * 提交故障工单反馈表单
     *
     * @param request
     * @param feedback
     * @param images
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    @RequestMapping(value = "/api/feedback/ticket", method = RequestMethod.POST)
    public JsonResult PostTicketFeedback(HttpServletRequest request, @Validated ClassifiedFeedback feedback, MultipartFile[] images) throws IOException, MessagingException {
        List<InputStream> inputStreamList = new ArrayList<>();
        if (images != null) {
            if (images.length > 9) {
                return new JsonResult(false, "不合法的图片文件");
            }
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty() || file.getSize() >= MAX_PICTURE_SIZE) {
                    return new JsonResult(false, "不合法的图片文件");
                }
                InputStream inputStream = file.getInputStream();
                inputStreamList.add(inputStream);
            }
        }
        feedbackService.SendTicketEmail(request.getSession().getId(), feedback.getContent(), feedback.getType()
                , inputStreamList.toArray(new InputStream[0]));
        return new JsonResult(true);
    }
}
