package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.exception.VerificationException.SendEmailException;
import cn.gdeiassistant.common.exception.VerificationException.VerificationCodeInvalidException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.email.controller.EmailController;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = EmailController.class)
@Order(value = 0)
public class EmailRestExceptionHandler {

    /**
     * 电子邮件验证码不匹配
     */
    @ExceptionHandler(VerificationCodeInvalidException.class)
    public ResponseEntity showVerificationCodeInvalidExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false, "验证码不正确"));
    }

    /**
     * 发送电子邮件验证码失败
     */
    @ExceptionHandler(SendEmailException.class)
    public ResponseEntity showSendEmailExceptionTip(SendEmailException e) {
        String message = (e.getMessage() != null && !e.getMessage().isEmpty())
                ? e.getMessage() : "发送验证码邮件出现异常";
        return ResponseEntity.ok(new JsonResult(false, message));
    }
}
