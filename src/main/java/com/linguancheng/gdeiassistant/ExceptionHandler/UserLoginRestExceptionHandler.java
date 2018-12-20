package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Constant.ConstantUtils;
import com.linguancheng.gdeiassistant.Controller.UserLogin.UserLoginRestController;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.RequestValidException.NonceInvalidException;
import com.linguancheng.gdeiassistant.Exception.RequestValidException.SignInvalidException;
import com.linguancheng.gdeiassistant.Exception.RequestValidException.TimestampInvalidException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserLoginRestController.class)
@Order(value = 0)
public class UserLoginRestExceptionHandler {

    /**
     * 处理账户密码错误异常
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity ShowPasswordIncorrectExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false, "用户名或密码错误，请重新输入"));
    }

    /**
     * 处理请求摘要签名校验失败异常
     *
     * @return
     */
    @ExceptionHandler(SignInvalidException.class)
    public ResponseEntity ShowSignInvalidExceptionTip() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResult(ConstantUtils.REQUEST_SIGN_INVALID
                , false, "请求的摘要签名不匹配"));
    }

    /**
     * 处理请求时间戳校验失败异常
     *
     * @return
     */
    @ExceptionHandler(TimestampInvalidException.class)
    public ResponseEntity ShowTimestampInvalidExceptionTip() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResult(ConstantUtils.REQUEST_TIMESTAMP_INVALID
                , false, "请求时间戳校验失败"));
    }

    /**
     * 处理请求随机值重复异常
     *
     * @return
     */
    @ExceptionHandler(NonceInvalidException.class)
    public ResponseEntity ShowNonceInvalidExceptionTip() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JsonResult(ConstantUtils.REPLAY_REQUEST
                , false, "重复提交的请求"));
    }
}
