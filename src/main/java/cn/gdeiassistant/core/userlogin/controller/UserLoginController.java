package cn.gdeiassistant.core.userLogin.controller;

import cn.gdeiassistant.common.annotation.DeviceUpdateRequirement;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.annotation.ReplayAttacksProtection;
import cn.gdeiassistant.common.annotation.RequestLogPersistence;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.pojo.Entity.*;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.userLogin.pojo.vo.UserLoginResultVO;
import cn.gdeiassistant.core.token.service.LoginTokenService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import cn.gdeiassistant.common.validgroup.Device.DeviceDataValidGroup;
import cn.gdeiassistant.common.validgroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private LoginTokenService loginTokenService;

    /**
     * 用户登录接口
     *
     * @param request
     * @param user
     * @param requestValidation
     * @return
     */
    @RequestMapping(value = "/api/userlogin/device", method = RequestMethod.POST)
    @RequestLogPersistence
    @ReplayAttacksProtection
    @DeviceUpdateRequirement
    @RecordIPAddress(type = IPAddressEnum.LOGIN, rest = true)
    public DataJsonResult<UserLoginResultVO> userLogin(HttpServletRequest request
            , @Validated(value = UserLoginValidGroup.class) User user
            , RequestValidation requestValidation
            , @Validated(value = DeviceDataValidGroup.class) Device device) throws Exception {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        userLoginService.userLogin(sessionId, user.getUsername(), user.getPassword());
        AccessToken accessToken = loginTokenService.getAccessToken(sessionId, user.getUsername());
        RefreshToken refreshToken = loginTokenService.getRefreshToken(accessToken);
        loginTokenService.saveDevice(accessToken.getSignature(), device);
        UserLoginResultVO vo = new UserLoginResultVO();
        vo.setUser(user);
        vo.setAccessToken(new Token(accessToken));
        vo.setRefreshToken(new Token(refreshToken));
        request.setAttribute("token", accessToken.getSignature());
        return new DataJsonResult<>(true, vo);
    }

    /**
     * 用户登录
     *
     * @param request
     * @param response
     * @param user
     * @return
     */
    @RequestMapping(value = "/api/userlogin")
    @RecordIPAddress(type = IPAddressEnum.LOGIN)
    public JsonResult userLogin(HttpServletRequest request, HttpServletResponse response
            , @Validated(value = UserLoginValidGroup.class) User user) throws Exception {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        //清除可能存在的同 cookie 关联的旧凭证（以 cookieId 为维度的清理需另处处理，此处仅保证本次会话独立）
        HttpClientUtils.clearHttpClientCookieStore(sessionId);
        //登录用户账号
        userLoginService.userLogin(sessionId, user.getUsername(), user.getPassword());
        //异步地与教务系统会话进行同步
        userCertificateService.asyncUpdateSessionCertificate(sessionId, user);
        //生成用户Cookie凭证并保存到缓存
        String cookieId = UUID.randomUUID().toString().replace("-", "");
        userCertificateService.saveUserCookieCertificate(cookieId, user.getUsername(), user.getPassword());
        //生成Cookie
        Cookie cookie = new Cookie("cookieId", cookieId);
        //设置Cookie最大有效时间为1个月
        cookie.setMaxAge(2592000);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return new JsonResult(true);
    }
}
