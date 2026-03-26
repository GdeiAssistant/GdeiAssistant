package cn.gdeiassistant.core.email.controller;

import cn.gdeiassistant.common.exception.VerificationException.SendEmailException;
import cn.gdeiassistant.common.exception.VerificationException.VerificationCodeInvalidException;
import cn.gdeiassistant.common.pojo.Entity.Email;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.core.email.service.EmailService;
import cn.gdeiassistant.common.annotation.RateLimit;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    private JsonResult failure(HttpServletRequest request, String message) {
        return new JsonResult(false, BackendTextLocalizer.localizeMessage(message, request != null ? request.getHeader("Accept-Language") : null));
    }

    /**
     * 获取电子邮件验证码
     *
     * @param request
     * @param email
     * @return
     */
    @RateLimit(maxRequests = 3, windowSeconds = 60)
    @RequestMapping(value = "/api/email/verification", method = RequestMethod.POST)
    public JsonResult getEmailVerificationCode(HttpServletRequest request
            , @Validated @NotBlank @Length(min = 5, max = 50) @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$") String email) throws SendEmailException {
        emailService.getEmailVerificationCode(email);
        return new JsonResult(true);
    }

    /**
     * 绑定电子邮件
     *
     * @param request
     * @param email
     * @return
     */
    @RequestMapping(value = "/api/email/bind", method = RequestMethod.POST)
    public JsonResult bindEmail(HttpServletRequest request
            , @Validated @NotBlank @Length(min = 5, max = 50) @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$") String email
            , @Validated @NotNull @Min(10000) @Max(999999) Integer randomCode) throws VerificationCodeInvalidException {
        emailService.checkVerificationCode(email, randomCode);
        String sessionId = (String) request.getAttribute("sessionId");
        emailService.bindUserEmail(sessionId, email);
        return new JsonResult(true);
    }

    /**
     * 解绑电子邮件
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/email/unbind", method = RequestMethod.POST)
    public JsonResult unBindEmail(HttpServletRequest request)  {
        String sessionId = (String) request.getAttribute("sessionId");
        Email email = emailService.queryUserEmail(sessionId);
        if (email != null) {
            emailService.unBindUserEmail(sessionId);
            return new JsonResult(true);
        }
        return failure(request, "当前用户未绑定电子邮件");
    }

    /**
     * 查询当前用户邮箱绑定状态
     * GET /api/email/status
     */
    @RequestMapping(value = "/api/email/status", method = RequestMethod.GET)
    public DataJsonResult<String> queryEmailStatus(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        Email email = emailService.queryUserEmail(sessionId);
        return new DataJsonResult<>(true, email != null ? email.getEmail() : null);
    }
}
