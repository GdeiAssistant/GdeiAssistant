package cn.gdeiassistant.ExceptionHandler;

import cn.gdeiassistant.Controller.AccountManagement.Phone.RestController.PhoneRestController;
import cn.gdeiassistant.Exception.VerificationException.*;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = PhoneRestController.class)
@Order(value = 0)
public class PhoneRestExceptionHandler {

    /**
     * 短信发送频率超过限制
     *
     * @return
     */
    @ExceptionHandler({HourFrequencyLimitException.class, MinuteFrequencyLimitException.class
            , DayFrequencyLimitException.class})
    public ResponseEntity ShowHourFrequencyLimitExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "发送短信次数超过限制，请稍后再试"));
    }

    /**
     * 不合法的手机号格式
     *
     * @return
     */
    @ExceptionHandler(IllegalPhoneNumberException.class)
    public ResponseEntity ShowIllegalPhoneNumberExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "手机号的格式不正确"));
    }

    /**
     * 验证码不匹配
     *
     * @return
     */
    @ExceptionHandler(VerificationCodeInvalidException.class)
    public ResponseEntity ShowVerificationCodeInvalidExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "验证码不正确"));
    }

    /**
     * 验证码不匹配
     *
     * @return
     */
    @ExceptionHandler(SendSMSException.class)
    public ResponseEntity ShowSendSMSExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "发送验证码短信出现异常"));
    }
}
