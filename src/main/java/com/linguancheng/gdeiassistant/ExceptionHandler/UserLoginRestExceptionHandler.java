package com.gdeiassistant.gdeiassistant.ExceptionHandler;

import com.gdeiassistant.gdeiassistant.Controller.UserLogin.UserLoginRestController;
import com.gdeiassistant.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.gdeiassistant.gdeiassistant.Pojo.Result.JsonResult;
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
