package cn.gdeiassistant.core.feedback.controller;

import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.pojo.entity.ClassifiedFeedback;
import cn.gdeiassistant.common.pojo.entity.Feedback;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.core.feedback.pojo.dto.FeedbackSubmitDTO;
import cn.gdeiassistant.core.feedback.service.FeedbackService;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.gdeiassistant.common.exception.verificationexception.SendEmailException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

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
        feedbackService.submitFeedback(sessionId, body);
        return new JsonResult(true, BackendTextLocalizer.localizeMessage("感谢您的反馈", request.getHeader("Accept-Language")));
    }

    /**
     * 提交意见建议反馈表单
     *
     * @param request
     * @param feedback
     * @return
     */
    @RequestMapping(value = "/api/feedback/function", method = RequestMethod.POST)
    public JsonResult postFunctionalFeedback(HttpServletRequest request, @Validated Feedback feedback, MultipartFile[] images) throws IOException, SendEmailException {
        byte[][] attachments = readAttachments(images);
        if (attachments == null) {
            return new JsonResult(false, "不合法的图片文件");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        feedbackService.sendFeedbackEmail(sessionId, feedback.getContent(), attachments);
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
     * @throws SendEmailException
     */
    @RequestMapping(value = "/api/feedback/ticket", method = RequestMethod.POST)
    public JsonResult postTicketFeedback(HttpServletRequest request, @Validated ClassifiedFeedback feedback, MultipartFile[] images) throws IOException, SendEmailException {
        byte[][] attachments = readAttachments(images);
        if (attachments == null) {
            return new JsonResult(false, "不合法的图片文件");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        feedbackService.sendTicketEmail(sessionId, feedback.getContent(), feedback.getType()
                , attachments);
        return new JsonResult(true);
    }
    /** Validate all files before opening streams; no request-owned resource leaves this method. */
    private byte[][] readAttachments(MultipartFile[] images) throws IOException {
        if (images == null) {
            return new byte[0][];
        }
        if (images.length > 9) {
            return null;
        }
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty() || file.getSize() >= ValueConstantUtils.MAX_IMAGE_SIZE) {
                return null;
            }
        }
        byte[][] attachments = new byte[images.length][];
        for (int i = 0; i < images.length; i++) {
            try (InputStream stream = images[i].getInputStream()) {
                attachments[i] = stream.readNBytes(ValueConstantUtils.MAX_IMAGE_SIZE);
            }
            if (attachments[i].length == 0 || attachments[i].length >= ValueConstantUtils.MAX_IMAGE_SIZE) {
                return null;
            }
        }
        return attachments;
    }
}
