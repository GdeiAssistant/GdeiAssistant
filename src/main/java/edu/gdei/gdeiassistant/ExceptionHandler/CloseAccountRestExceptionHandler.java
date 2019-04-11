package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Account.CloseAccountRestController;
import edu.gdei.gdeiassistant.Exception.CloseAccountException.ItemAvailableException;
import edu.gdei.gdeiassistant.Exception.CloseAccountException.UserStateErrorException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CloseAccountRestController.class)
@Order(value = 1)
public class CloseAccountRestExceptionHandler {

    /**
     * 处理用户账号状态异常的错误
     *
     * @return
     */
    @ExceptionHandler(UserStateErrorException.class)
    public ResponseEntity HandleUserStateErrorException() {
        return ResponseEntity.ok(new JsonResult(false, "用户账号状态异常，请联系管理员"));
    }

    /**
     * 处理用户账号密码不匹配的异常
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException() {
        return ResponseEntity.ok(new JsonResult(false, "用户账号密码不匹配，请重试"));
    }

    /**
     * 处理用户有待处理的社区功能信息的异常
     *
     * @return
     */
    @ExceptionHandler(ItemAvailableException.class)
    public ResponseEntity HandleItemAvailableException() {
        return ResponseEntity.ok(new JsonResult(false, "用户有待处理的社区功能信息，不满足注销条件"));
    }

}
