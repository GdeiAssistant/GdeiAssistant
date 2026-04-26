package cn.gdeiassistant.core.userLogin.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.tools.Utils.JwtUtil;
import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import cn.gdeiassistant.core.campuscredential.pojo.dto.CampusCredentialConsentDTO;
import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.user.pojo.dto.UserLoginDTO;
import cn.gdeiassistant.core.userData.service.UserDataService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
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
    private CampusCredentialService campusCredentialService;

    @Autowired
    private UserDataService userDataService;

    /**
     * 用户登录（JWT）
     * 账号密码校验通过后签发 JWT，载荷包含用户唯一标识（学号 username）。
     * 返回格式：{ "code": 200, "message": "success", "data": { "token": "eyJhb..." } }
     */
    @PostMapping("/login")
    @RateLimit(maxRequests = 5, windowSeconds = 60)
    public DataJsonResult<Map<String, String>> login(
            @Validated(UserLoginValidGroup.class) @RequestBody UserLoginDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) {
        String language = request != null ? request.getHeader("Accept-Language") : null;
        String username = dto.getUsername();
        String password = dto.getPassword();
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        boolean persistCredential = Boolean.TRUE.equals(dto.getCampusCredentialConsent());
        boolean consentPersisted = !persistCredential;
        try {
            userLoginService.userLogin(sessionId, username, password, false);
        } catch (PasswordIncorrectException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            DataJsonResult<Map<String, String>> err = new DataJsonResult<>(false, null);
            err.setCode(ErrorConstantUtils.PASSWORD_INCORRECT);
            err.setMessage(BackendTextLocalizer.localizeMessage(
                    e.getMessage() != null ? e.getMessage() : "账号或密码错误",
                    language));
            return err;
        } catch (Exception e) {
            logger.error("登录过程发生异常", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            DataJsonResult<Map<String, String>> err = new DataJsonResult<>(false, null);
            err.setCode(ErrorConstantUtils.INTERNAL_SERVER_ERROR);
            err.setMessage(BackendTextLocalizer.localizeMessage("系统繁忙，登录失败，请稍后重试", language));
            return err;
        }
        if (persistCredential) {
            try {
                CampusCredentialConsentDTO consentDTO = new CampusCredentialConsentDTO();
                consentDTO.setScene(dto.getConsentScene());
                consentDTO.setPolicyDate(dto.getPolicyDate());
                consentDTO.setEffectiveDate(dto.getEffectiveDate());
                campusCredentialService.recordConsentByUsername(username, consentDTO);
                userDataService.syncUserData(new User(username, password), true);
                consentPersisted = true;
            } catch (Exception e) {
                String maskedUsername = String.valueOf(AnonymizeUtils.maskUsername(username))
                        .replace('\n', '_')
                        .replace('\r', '_');
                logger.warn("登录成功后记录校园凭证授权失败，username={}", maskedUsername, e);
            }
        }
        String jwt = jwtUtil.createToken(sessionId, username);
        Map<String, String> data = new HashMap<>();
        data.put("token", jwt);
        if (persistCredential) {
            data.put("campusCredentialConsentPersisted", String.valueOf(consentPersisted));
        }
        DataJsonResult<Map<String, String>> result = new DataJsonResult<>(true, data);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    /**
     * 轻量 session/token 校验端点。
     * JWT 签名和 Redis session 有效性由 JwtSessionIdFilter + ApiAuthInterceptor 保证——
     * 请求到达此方法时 token 必然有效。不查询用户资料，返回标准成功响应。
     * 返回格式：{ "code": 200, "message": "success", "success": true, "data": null }
     */
    @GetMapping("/validate")
    public DataJsonResult<Void> validate() {
        DataJsonResult<Void> result = new DataJsonResult<>(true, null);
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
            HttpClientUtils.clearHttpClientCookieStore(sessionId);
            userCertificateService.clearUserLoginAndSession(sessionId);
        }
        DataJsonResult<Void> result = new DataJsonResult<>(true, null);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }
}
