package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Constant.ConstantUtils;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.TokenExpiredException;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.TokenNotMatchingException;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.TokenServerException;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.UnusualLocationException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/11
 */
@RestControllerAdvice
@Order(value = Integer.MIN_VALUE)
public class TokenExceptionHandler {

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity HandleTokenExpiredException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.TOKEN_EXPIRED_EXCEPTION
                , false, "登录凭证已过期，请重新登录"));
    }

    @ExceptionHandler(UnusualLocationException.class)
    public ResponseEntity HandleUnusualLocationException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.UNUSUAL_LOCATION_EXCEPTION
                , false, "登录地点异常，请进行身份验证"));
    }

    @ExceptionHandler(TokenNotMatchingException.class)
    public ResponseEntity HandleTokenNotMatchingException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.TOKEN_NOT_MATCHING
                , false, "没有对应的登录凭证记录，请尝试重新登录"));
    }

    @ExceptionHandler(TokenServerException.class)
    public ResponseEntity HandleTokenServerException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.TOKEN_SERVER_ERROR
                , false, "登录凭证校验服务异常，请联系管理员"));
    }
}
