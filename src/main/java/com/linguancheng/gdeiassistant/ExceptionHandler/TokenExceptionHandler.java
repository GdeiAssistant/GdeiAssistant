package com.gdeiassistant.gdeiassistant.ExceptionHandler;

import com.gdeiassistant.gdeiassistant.Constant.ConstantUtils;
import com.gdeiassistant.gdeiassistant.Exception.TokenValidException.TokenExpiredException;
import com.gdeiassistant.gdeiassistant.Exception.TokenValidException.TokenNotMatchingException;
import com.gdeiassistant.gdeiassistant.Exception.TokenValidException.TokenServerException;
import com.gdeiassistant.gdeiassistant.Exception.TokenValidException.UnusualLocationException;
import com.gdeiassistant.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/11
 */
@RestControllerAdvice
public class TokenExceptionHandler {

    @ExceptionHandler(TokenExpiredException.class)
    public JsonResult HandleTokenExpiredException() {
        return new JsonResult(ConstantUtils.TOKEN_EXPIRED_EXCEPTION, false, "权限令牌已过期，请重新登录");
    }

    @ExceptionHandler(UnusualLocationException.class)
    public JsonResult HandleUnusualLocationException() {
        return new JsonResult(ConstantUtils.UNUSUAL_LOCATION_EXCEPTION, false, "登录地点异常，请进行身份验证");
    }

    @ExceptionHandler(TokenNotMatchingException.class)
    public JsonResult HandleTokenNotMatchingException() {
        return new JsonResult(ConstantUtils.TOKEN_NOT_MATCHING, false, "令牌信息不匹配");
    }

    @ExceptionHandler(TokenServerException.class)
    public JsonResult HandleTokenServerException() {
        return new JsonResult(ConstantUtils.TOKEN_SERVER_ERROR, false, "令牌校验服务异常");
    }
}
