package edu.gdei.gdeiassistant.Controller.UserLogin;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Annotation.ReplayAttacksProtection;
import edu.gdei.gdeiassistant.Annotation.RequestLogPersistence;
import edu.gdei.gdeiassistant.Enum.Method.LoginMethodEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Pojo.UserLogin.UserCertificate;
import edu.gdei.gdeiassistant.Pojo.UserLogin.UserLoginResult;
import edu.gdei.gdeiassistant.Service.IPAddress.IPService;
import edu.gdei.gdeiassistant.Service.Token.LoginTokenService;
import edu.gdei.gdeiassistant.Service.UserData.UserDataService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import edu.gdei.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserLoginRestController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private IPService ipService;

    /**
     * 用户登录接口
     *
     * @param request
     * @param user
     * @param unionid
     * @param method
     * @param requestValidation
     * @return
     */
    @RequestMapping(value = "/rest/userlogin", method = RequestMethod.POST)
    @RequestLogPersistence
    @ReplayAttacksProtection
    public DataJsonResult<UserLoginResult> UserLogin(HttpServletRequest request, @Validated(value = UserLoginValidGroup.class) User user
            , @RequestParam(value = "unionid", required = false) String unionid
            , @RequestParam(value = "method", required = false, defaultValue = "0") LoginMethodEnum method
            , RequestValidation requestValidation) throws Exception {
        UserCertificate userCertificate = null;
        switch (method) {
            case QUICK_LOGIN:
                userCertificate = userLoginService.UserLogin(request.getSession().getId(), user, true);
                break;

            case CAS_LOGIN:
                userCertificate = userLoginService.UserLogin(request.getSession().getId(), user, false);
                break;
        }
        User resultUser = userCertificate.getUser();
        //同步数据库用户数据
        userDataService.SyncUserData(resultUser);
        //获取权限令牌和刷新令牌
        AccessToken accessToken = loginTokenService.GetAccessToken(user.getUsername(), ipService.GetRequestRealIPAddress(request), unionid);
        RefreshToken refreshToken = loginTokenService.GetRefreshToken(accessToken);
        UserLoginResult userLoginResult = new UserLoginResult();
        userLoginResult.setUser(resultUser);
        userLoginResult.setAccessToken(new Token(accessToken));
        userLoginResult.setRefreshToken(new Token(refreshToken));
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
        UserCertificate userCertificate = userLoginService.UserLogin(request.getSession().getId(), user, true);
        //同步数据库用户数据
        userDataService.SyncUserData(userCertificate.getUser());
        //将用户信息数据写入Session
        request.getSession().setAttribute("username", userCertificate.getUser().getUsername());
        request.getSession().setAttribute("group", userCertificate.getUser().getGroup());
        //同步教务系统会话
        userLoginService.AsyncUpdateSession(request);
        //将加密的用户信息保存到Cookie中
        String username = StringEncryptUtils.encryptString(userCertificate.getUser().getUsername());
        String password = StringEncryptUtils.encryptString(userCertificate.getUser().getPassword());
        Cookie usernameCookie = new Cookie("username", username);
        Cookie passwordCookie = new Cookie("password", password);
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
