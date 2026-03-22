package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.exception.TokenValidException.SuspiciouseRequestException;
import cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException;
import cn.gdeiassistant.common.exception.TokenValidException.TokenNotMatchingException;
import cn.gdeiassistant.common.pojo.Entity.Device;
import cn.gdeiassistant.core.token.service.LoginTokenService;
import cn.gdeiassistant.common.tools.Utils.IPAddressUtils;
import cn.gdeiassistant.common.tools.Utils.JwtUtil;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.exceptions.JWTVerificationException;
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
import java.util.Map;

@Aspect
@Component
@Order(1)
public class LoginTokenAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoginTokenAspect.class);

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private JwtUtil jwtUtil;

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
        // 仅接受标准 Authorization: Bearer xxx 头
        String token = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.toLowerCase().startsWith("bearer ")) {
            token = auth.substring(7).trim();
        }
        if (token == null || token.trim().isEmpty()) {
            throw new TokenExpiredException("请求未携带有效令牌或令牌已过期");
        }

        // 1. 优先使用 JwtSessionIdFilter 已解析的 sessionId
        String sessionId = (String) request.getAttribute("sessionId");

        if (sessionId != null && !sessionId.isEmpty()) {
            // Filter 已完成 JWT 签名校验与 sessionId 解析，再做一次签名校验确保一致性
            loginTokenService.validToken(token);
        } else {
            // 2. Filter 未注入 sessionId，自行解析 JWT
            try {
                Map<String, Claim> claims = jwtUtil.verifyAndParse(token);
                Claim sessionIdClaim = claims.get("sessionId");
                if (sessionIdClaim != null && !sessionIdClaim.isNull()) {
                    sessionId = sessionIdClaim.asString();
                }
            } catch (JWTVerificationException e) {
                throw new TokenNotMatchingException("签名验证不通过");
            }

            if (sessionId == null || sessionId.isEmpty()) {
                throw new TokenExpiredException("无法解析会话标识，令牌已过期或无效");
            }

            loginTokenService.validToken(token);
        }

        // 可选设备校验：优先从方法参数获取，其次从 X-Device-ID 请求头获取
        Device requestDevice = null;
        int deviceIndex = ArrayUtils.indexOf(((MethodSignature) joinPoint.getSignature()).getParameterNames(), "device");
        if (deviceIndex >= 0 && deviceIndex < args.length) {
            requestDevice = (Device) args[deviceIndex];
        }
        // 若方法参数中没有设备信息，尝试从 X-Device-ID 请求头构造（移动客户端场景）
        if (requestDevice == null || requestDevice.getUnionID() == null) {
            String deviceId = request.getHeader("X-Device-ID");
            if (deviceId != null && !deviceId.trim().isEmpty()) {
                requestDevice = new Device();
                requestDevice.setUnionID(deviceId.trim());
            }
        }
        if (requestDevice != null && requestDevice.getUnionID() != null) {
            String ip = IPAddressUtils.getRequestRealIpAddress(request);
            loginTokenService.validDevice(token, ip, requestDevice);
        }

        // 注入 sessionId（覆盖写入，确保后续 Controller 可用）
        request.setAttribute("sessionId", sessionId);
    }
}
