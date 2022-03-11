package cn.gdeiassistant.Controller.UserLogin.RestController;

import cn.gdeiassistant.Annotation.DeviceUpdateRequirement;
import cn.gdeiassistant.Annotation.ReplayAttacksProtection;
import cn.gdeiassistant.Annotation.RequestLogPersistence;
import cn.gdeiassistant.Pojo.Entity.*;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Pojo.UserLogin.UserLoginResult;
import cn.gdeiassistant.Service.Token.LoginTokenService;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Service.UserLogin.UserLoginService;
import cn.gdeiassistant.Tools.Utils.HttpClientUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import cn.gdeiassistant.ValidGroup.Device.DeviceDataValidGroup;
import cn.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserLoginRestController {

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
    @RequestMapping(value = "/rest/userlogin", method = RequestMethod.POST)
    @RequestLogPersistence
    @ReplayAttacksProtection
    @DeviceUpdateRequirement
    public DataJsonResult<UserLoginResult> UserLogin(HttpServletRequest request
            , @Validated(value = UserLoginValidGroup.class) User user
            , RequestValidation requestValidation
            , @Validated(value = DeviceDataValidGroup.class) Device device) throws Exception {
        //用户账号登录
        userLoginService.UserLogin(request.getSession().getId()
                , user.getUsername(), user.getPassword());
        //获取权限令牌和刷新令牌
        AccessToken accessToken = loginTokenService.GetAccessToken(request.getSession().getId()
                , user.getUsername());
        RefreshToken refreshToken = loginTokenService.GetRefreshToken(accessToken);
        //保存设备信息
        loginTokenService.SaveDevice(accessToken.getSignature(), device);
        UserLoginResult userLoginResult = new UserLoginResult();
        userLoginResult.setUser(user);
        userLoginResult.setAccessToken(new Token(accessToken));
        userLoginResult.setRefreshToken(new Token(refreshToken));
        //将令牌签名写入Request作用域，LoginTokenAspect将保存设备信息到缓存
        request.setAttribute("token", accessToken.getSignature());
        return new DataJsonResult<>(true, userLoginResult);
    }

    /**
     * 用户登录
     *
     * @param request
     * @param response
     * @param user
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/userlogin")
    public JsonResult UserLogin(HttpServletRequest request, HttpServletResponse response
            , @Validated(value = UserLoginValidGroup.class) User user) throws Exception {
        //清除已登录用户的用户凭证记录
        HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
        //登录用户账号
        userLoginService.UserLogin(request.getSession().getId(), user.getUsername(), user.getPassword());
        //异步地与教务系统会话进行同步
        userCertificateService.AsyncUpdateSessionCertificate(request.getSession().getId()
                , user);
        //将加密的用户信息保存到Cookie中
        Cookie usernameCookie = new Cookie("username", StringEncryptUtils
                .encryptString(user.getUsername()));
        Cookie passwordCookie = new Cookie("password", StringEncryptUtils
                .encryptString(user.getPassword()));
        //设置Cookie最大有效时间为3个月
        usernameCookie.setMaxAge(7776000);
        usernameCookie.setPath("/");
        usernameCookie.setHttpOnly(true);
        passwordCookie.setMaxAge(7776000);
        passwordCookie.setPath("/");
        passwordCookie.setHttpOnly(true);
        response.addCookie(usernameCookie);
        response.addCookie(passwordCookie);
        return new JsonResult(true);
    }
}
