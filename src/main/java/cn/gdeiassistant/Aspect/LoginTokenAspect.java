package cn.gdeiassistant.Aspect;

import cn.gdeiassistant.Exception.TokenValidException.SuspiciouseRequestException;
import cn.gdeiassistant.Exception.TokenValidException.TokenExpiredException;
import cn.gdeiassistant.Exception.TokenValidException.TokenNotMatchingException;
import cn.gdeiassistant.Pojo.Entity.Device;
import cn.gdeiassistant.Service.Token.LoginTokenService;
import cn.gdeiassistant.Tools.Utils.IPAddressUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class LoginTokenAspect {

    @Autowired
    private LoginTokenService loginTokenService;

    @Pointcut("@annotation(cn.gdeiassistant.Annotation.DeviceUpdateRequirement)")
    public void LoginAction() {

    }

    @Pointcut("@annotation(cn.gdeiassistant.Annotation.RestAuthentication)")
    public void QueryAction() {

    }

    @Before("LoginAction()")
    public void GetIPAddress(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        //获取设备信息
        Device device = (Device) args[ArrayUtils.indexOf(((MethodSignature) joinPoint.getSignature())
                .getParameterNames(), "device")];
        //获取IP地址信息
        String ip = IPAddressUtils.GetRequestRealIPAddress(request);
        device.setIp(ip);
    }

    @After("LoginAction()")
    public void UpdateDevice(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        //获取设备信息
        Device device = (Device) args[ArrayUtils.indexOf(((MethodSignature) joinPoint.getSignature())
                .getParameterNames(), "device")];
        //获取权限令牌签名
        String signature = (String) request.getAttribute("token");
        //保存设备信息
        loginTokenService.SaveDevice(signature, device);
    }

    @Before("QueryAction()")
    public void AuthenticateToken(JoinPoint joinPoint) throws TokenExpiredException
            , TokenNotMatchingException, SuspiciouseRequestException {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        //获取用户请求的权限令牌签名
        String token = request.getParameter("token");
        //验证令牌有效期
        loginTokenService.ValidExpiration(token);
        //获取设备信息
        Device device = (Device) args[ArrayUtils.indexOf(((MethodSignature) joinPoint.getSignature())
                .getParameterNames(), "device")];
        //获取请求IP地址
        String ip = IPAddressUtils.GetRequestRealIPAddress(request);
        //校验令牌访问的设备信息
        loginTokenService.ValidDevice(token, ip, device);
        //校验令牌信息
        loginTokenService.ValidToken(token);
        //校验令牌通过
        String sessionId = loginTokenService.ParseToken(token).get("sessionId").asString();
        request.setAttribute("sessionId", sessionId);
    }
}
