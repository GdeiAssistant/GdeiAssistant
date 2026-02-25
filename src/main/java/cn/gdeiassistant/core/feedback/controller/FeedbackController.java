package cn.gdeiassistant.core.feedback.controller;

import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.pojo.Entity.ClassifiedFeedback;
import cn.gdeiassistant.common.pojo.Entity.Feedback;
import cn.gdeiassistant.core.feedback.pojo.dto.FeedbackSubmitDTO;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.feedback.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 帮助与反馈 - 提交反馈（JSON  body，写入 MySQL）
     * POST /api/feedback
     *
     * @param request HTTP 请求（取 session）
     * @param body    content 必填，contact、type 选填
     * @return JsonResult(true, "感谢您的反馈")
     */
    @RequestMapping(value = "/api/feedback", method = RequestMethod.POST)
    public JsonResult postFeedback(HttpServletRequest request, @RequestBody @Validated FeedbackSubmitDTO body) {
        String sessionId = (String) request.getAttribute("sessionId");
        feedbackService.SubmitFeedback(sessionId, body);
        return new JsonResult(true, "感谢您的反馈");
    }

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
                if (file == null || file.isEmpty() || file.getSize() >= ValueConstantUtils.MAX_IMAGE_SIZE) {
                    return new JsonResult(false, "不合法的图片文件");
                }
                InputStream inputStream = file.getInputStream();
                inputStreamList.add(inputStream);
            }
        }
        String sessionId = (String) request.getAttribute("sessionId");
        feedbackService.SendFeedbackEmail(sessionId, feedback.getContent(), inputStreamList.toArray(new InputStream[0]));
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
                if (file == null || file.isEmpty() || file.getSize() >= ValueConstantUtils.MAX_IMAGE_SIZE) {
                    return new JsonResult(false, "不合法的图片文件");
                }
                InputStream inputStream = file.getInputStream();
                inputStreamList.add(inputStream);
            }
        }
        String sessionId = (String) request.getAttribute("sessionId");
        feedbackService.SendTicketEmail(sessionId, feedback.getContent(), feedback.getType()
                , inputStreamList.toArray(new InputStream[0]));
        return new JsonResult(true);
    }
}
