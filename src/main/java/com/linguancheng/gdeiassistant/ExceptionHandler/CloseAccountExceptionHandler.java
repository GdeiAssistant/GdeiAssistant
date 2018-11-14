package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Controller.CloseAccount.CloseAccountController;
import com.linguancheng.gdeiassistant.Exception.CloseAccountException.ItemAvailableException;
import com.linguancheng.gdeiassistant.Exception.CloseAccountException.UserStateErrorException;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CloseAccountController.class)
public class CloseAccountExceptionHandler {

    @ExceptionHandler(UserStateErrorException.class)
    public ResponseEntity HandleUserStateErrorException() {
        return ResponseEntity.ok(new JsonResult(false, "用户账号状态异常，请联系管理员"));
    }

    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException() {
        return ResponseEntity.ok(new JsonResult(false, "用户账号密码不匹配，请重试"));
    }

    @ExceptionHandler(ItemAvailableException.class)
    public ResponseEntity HandleItemAvailableException() {
        return ResponseEntity.ok(new JsonResult(false, "用户有待处理的社区功能信息，不满足注销条件"));
    }

}
