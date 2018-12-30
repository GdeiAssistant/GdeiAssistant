package com.linguancheng.gdeiassistant.Aspect;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.TokenValidResultEnum;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.TokenExpiredException;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.TokenNotMatchingException;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.TokenServerException;
import com.linguancheng.gdeiassistant.Exception.TokenValidException.UnusualLocationException;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.IPAddress.IPService;
import com.linguancheng.gdeiassistant.Service.Token.LoginTokenService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/4
 */
@Aspect
@Component
@Order(1)
public class LoginTokenAspect {

    @Autowired
    private IPService ipService;

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private UserLoginService userLoginService;

    @Pointcut("@annotation(com.linguancheng.gdeiassistant.Annotation.RestAuthentication)")
    public void QueryAction() {

    }

    @Before("QueryAction()")
    public void UserAuthenticate(JoinPoint joinPoint) throws Exception {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        //获取用户请求的权限令牌签名
        String token = (String) args[1];
        //获取用户请求的IP地址
        String ip = ipService.GetRequestRealIPAddress(request);
        //校验令牌信息
        TokenValidResultEnum tokenValidResultEnum = loginTokenService
                .ValidToken(token, ip);
        switch (tokenValidResultEnum) {
            case SUCCESS:
                //校验令牌通过
                BaseResult<User, DataBaseResultEnum> result = userLoginService
                        .GetUserByUsername(loginTokenService.ParseToken(token).get("username").asString());
                switch (result.getResultType()) {
                    case SUCCESS:
                        //登录成功
                        request.setAttribute("user", result.getResultData());
                        break;

                    case ERROR:
                        //系统异常
                        request.setAttribute("errorType", TokenServerException.class.getSimpleName());
                        throw new TokenServerException("令牌校验服务出现异常");

                    case EMPTY_RESULT:
                        //没有找到用户名对应的用户账号
                        request.setAttribute("errorType", TokenNotMatchingException.class.getSimpleName());
                        throw new TokenNotMatchingException("令牌信息不匹配");
                }
                break;

            case ERROR:
                //系统异常
                request.setAttribute("errorType", TokenServerException.class.getSimpleName());
                throw new TokenServerException("令牌校验服务出现异常");

            case EXPIRED:
                //令牌过期
                request.setAttribute("errorType", TokenExpiredException.class.getSimpleName());
                throw new TokenExpiredException("令牌已过期，请重新获取");

            case NOT_MATCH:
                //令牌信息不匹配
                request.setAttribute("errorType", TokenNotMatchingException.class.getSimpleName());
                throw new TokenNotMatchingException("令牌信息不匹配");

            case UNUSUAL_LOCATION:
                //异常登录地
                request.setAttribute("errorType", UnusualLocationException.class.getSimpleName());
                throw new UnusualLocationException("异常登录地点，请重新验证用户身份");
        }
    }

}
