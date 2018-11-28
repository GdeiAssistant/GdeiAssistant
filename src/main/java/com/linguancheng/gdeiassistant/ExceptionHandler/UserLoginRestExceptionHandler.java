package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Controller.UserLogin.UserLoginRestController;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserLoginRestController.class)
public class UserLoginRestExceptionHandler {

    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity ShowPasswordIncorrectExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false, "用户名或密码错误，请重新输入"));
    }
}
