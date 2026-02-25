package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.exception.TokenValidException.SuspiciouseRequestException;
import cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException;
import cn.gdeiassistant.common.exception.TokenValidException.TokenNotMatchingException;
import cn.gdeiassistant.common.pojo.Entity.Device;
import cn.gdeiassistant.common.redis.LoginToken.LoginTokenDao;
import cn.gdeiassistant.core.token.service.LoginTokenService;
import cn.gdeiassistant.common.tools.Utils.IPAddressUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class LoginTokenAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoginTokenAspect.class);

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private LoginTokenDao loginTokenDao;

    @Pointcut("@annotation(cn.gdeiassistant.common.annotation.DeviceUpdateRequirement)")
    public void LoginAction() {

    }

    @Pointcut("@annotation(cn.gdeiassistant.common.annotation.RestAuthentication)")
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
        String ip = IPAddressUtils.getRequestRealIpAddress(request);
        device.setIP(ip);
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
        loginTokenService.saveDevice(signature, device);
    }

    @Before("QueryAction()")
    public void AuthenticateToken(JoinPoint joinPoint) throws TokenExpiredException
            , TokenNotMatchingException, SuspiciouseRequestException {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        // 1. 严格执行前后端分离规范：仅从 Header 获取 Token
        String token = request.getHeader("token");
        // 2. 支持标准的 Authorization: Bearer xxx 格式
        if (token == null || token.trim().isEmpty()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.toLowerCase().startsWith("bearer ")) {
                token = auth.substring(7).trim();
            }
        }
        // 3. 拒绝一切 URL 传参，如果没有获取到，直接抛出异常
        if (token == null || token.trim().isEmpty()) {
            throw new TokenExpiredException("请求未携带有效令牌或令牌已过期");
        }
        // 验证令牌有效期
        loginTokenService.validExpiration(token);

        String ip = IPAddressUtils.getRequestRealIpAddress(request);
        // 核心安全屏障：向 Redis 查底账，判断该 Token 发放时是否绑定了设备
        Device boundDevice = loginTokenDao.QueryDeviceData(token);

        if (boundDevice != null) {
            // 【Redis 有记录 = APP 登录】：必须执行设备指纹强校验
            int deviceIndex = ArrayUtils.indexOf(((MethodSignature) joinPoint.getSignature()).getParameterNames(), "device");
            if (deviceIndex >= 0 && deviceIndex < args.length) {
                Device requestDevice = (Device) args[deviceIndex];
                if (requestDevice != null) {
                    loginTokenService.validDevice(token, ip, requestDevice);
                } else {
                    throw new SuspiciouseRequestException("APP端高危操作缺少设备特征码");
                }
            } else {
                throw new SuspiciouseRequestException("后端接口未配置设备接收参数，拒绝强校验");
            }
        } else {
            // 【Redis 无记录 = WEB 登录】：无设备硬件码，放行至 Controller，依托业务密码进行二次核验
            logger.debug("Web 端高危操作，依托业务层二次密码校验");
        }

        // 全端通用校验：JWT 防伪造、防篡改校验 (Web 和 App 都要走)
        loginTokenService.validToken(token);

        // 4. 注入 SessionId
        String sessionId = loginTokenService.parseToken(token).get("sessionId").asString();
        request.setAttribute("sessionId", sessionId);
    }
}
