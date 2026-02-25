package cn.gdeiassistant.core.userLogin.controller;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.core.user.pojo.dto.UserLoginDTO;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.redis.LoginToken.LoginTokenDao;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import cn.gdeiassistant.common.tools.Utils.JwtUtil;
import cn.gdeiassistant.common.validgroup.User.UserLoginValidGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 前后端分离 JWT 鉴权：登录/登出接口，不依赖 HttpSession，返回 JWT Token。
 * 路径为 /api/auth（登录 POST /api/auth/login、登出 POST /api/auth/logout），
 * 已在 ApiAuthInterceptor 白名单 /api/auth 下放行，无 Filter 对本 Controller 做重定向。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginTokenDao loginTokenDao;

    /**
     * 用户登录（JWT）
     * 账号密码校验通过后签发 JWT，载荷包含用户唯一标识（学号 username）。
     * 返回格式：{ "code": 200, "message": "success", "data": { "token": "eyJhb..." } }
     */
    @PostMapping("/login")
    public DataJsonResult<Map<String, String>> login(
            @Validated(UserLoginValidGroup.class) @RequestBody UserLoginDTO dto,
            HttpServletResponse response) {
        String username = dto.getUsername();
        String password = dto.getPassword();
        // 可撤销 Stateful JWT：sessionId 与用户凭证存 Redis；再生成唯一 token，写入 Redis token->sessionId，JWT 仅携带该 token
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        try {
            userLoginService.userLogin(sessionId, username, password);
        } catch (PasswordIncorrectException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            DataJsonResult<Map<String, String>> err = new DataJsonResult<>(false, null);
            err.setCode(ErrorConstantUtils.PASSWORD_INCORRECT);
            err.setMessage(e.getMessage() != null ? e.getMessage() : "账号或密码错误");
            return err;
        } catch (Exception e) {
            logger.error("登录过程发生异常", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            DataJsonResult<Map<String, String>> err = new DataJsonResult<>(false, null);
            err.setCode(ErrorConstantUtils.INTERNAL_SERVER_ERROR);
            err.setMessage("系统繁忙，登录失败，请稍后重试");
            return err;
        }
        String loginToken = UUID.randomUUID().toString().replace("-", "");
        loginTokenDao.InsertWebLoginToken(loginToken, sessionId);
        String jwt = jwtUtil.createTokenWithLoginToken(loginToken, username);
        Map<String, String> data = new HashMap<>();
        data.put("token", jwt);
        DataJsonResult<Map<String, String>> result = new DataJsonResult<>(true, data);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    /**
     * 退出登录：根据当前请求的 sessionId（由 JwtSessionIdFilter 注入）清理 Redis 中的登录/会话凭证，使该 JWT 事实失效。
     * 返回标准 JSON：{ "code": 200, "message": "success", "data": null }。
     */
    @PostMapping("/logout")
    public DataJsonResult<Void> logout(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        if (sessionId != null && !sessionId.isEmpty()) {
            loginTokenDao.DeleteWebLoginTokenBySessionId(sessionId);
            HttpClientUtils.clearHttpClientCookieStore(sessionId);
            userCertificateService.clearUserLoginAndSession(sessionId);
        }
        DataJsonResult<Void> result = new DataJsonResult<>(true, null);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }
}
