package cn.gdeiassistant.Controller.AccountManagement.Email.RestController;

import cn.gdeiassistant.Exception.VerificationException.VerificationCodeInvalidException;
import cn.gdeiassistant.Pojo.Entity.Email;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AccountManagement.Email.EmailService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@RestController
public class EmailRestController {

    @Autowired
    private EmailService emailService;

    /**
     * 获取电子邮件验证码
     *
     * @param request
     * @param email
     * @return
     */
    @RequestMapping(value = "/api/email/verification", method = RequestMethod.POST)
    public JsonResult GetEmailVerificationCode(HttpServletRequest request
            , @Validated @NotBlank @Length(min = 5, max = 50) @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$") String email) {
        emailService.GetEmailVerificationCode(email);
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
    public JsonResult BindEmail(HttpServletRequest request
            , @Validated @NotBlank @Length(min = 5, max = 50) @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$") String email
            , @Validated @NotNull @Min(10000) @Max(999999) Integer randomCode) throws VerificationCodeInvalidException {
        emailService.CheckVerificationCode(email, randomCode);
        emailService.BindUserEmail(request.getSession().getId(), email);
        return new JsonResult(true);
    }

    /**
     * 解绑电子邮件
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/email/unbind", method = RequestMethod.POST)
    public JsonResult UnBindEmail(HttpServletRequest request)  {
        Email email = emailService.QueryUserEmail(request.getSession().getId());
        if (email != null) {
            emailService.UnBindUserEmail(request.getSession().getId());
            return new JsonResult(true);
        }
        return new JsonResult(false, "当前用户未绑定电子邮件");
    }
}
