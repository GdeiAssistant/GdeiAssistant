package com.linguancheng.gdeiassistant.Controller.UserLogin;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.LoginMethodEnum;
import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.TokenValidResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.TransactionException;
import com.linguancheng.gdeiassistant.Pojo.Entity.*;
import com.linguancheng.gdeiassistant.Pojo.Redirect.RedirectInfo;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Pojo.TokenRefresh.TokenRefreshJsonResult;
import com.linguancheng.gdeiassistant.Pojo.TokenRefresh.TokenRefreshResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserLoginJsonResult;
import com.linguancheng.gdeiassistant.Service.IPAddress.IPService;
import com.linguancheng.gdeiassistant.Service.Profile.UserProfileService;
import com.linguancheng.gdeiassistant.Service.Token.LoginTokenService;
import com.linguancheng.gdeiassistant.Service.UserData.UserDataService;
import com.linguancheng.gdeiassistant.Tools.HttpClientUtils;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import com.linguancheng.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * Created by linguancheng on 2017/7/16.
 */

@Controller
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private IPService ipService;

    /**
     * 进入登录界面
     *
     * @param request
     * @param redirectInfo
     * @return
     */
    @RequestMapping(value = "/login")
    public ModelAndView ResolveLoginPage(HttpServletRequest request, RedirectInfo redirectInfo) {
        HttpSession httpSession = request.getSession();
        if (StringUtils.isBlank((String) httpSession.getAttribute("username"))) {
            ModelAndView modelAndView = new ModelAndView("Login/login");
            if (redirectInfo.needToRedirect()) {
                modelAndView.addObject("RedirectURL", redirectInfo.getRedirect_url());
            }
            return modelAndView;
        }
        if (redirectInfo != null && redirectInfo.needToRedirect()) {
            return new ModelAndView("redirect:" + redirectInfo.getRedirect_url());
        }
        return new ModelAndView("redirect:/index");
    }

    /**
     * 使令牌信息主动过期
     *
     * @param signature
     * @return
     */
    @RequestMapping(value = "/rest/token/expire", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult ExpireToken(@RequestParam("token") String signature) {
        JsonResult jsonResult = new JsonResult();
        TokenValidResultEnum tokenValidResultEnum = loginTokenService.ExpireToken(signature);
        switch (tokenValidResultEnum) {
            case SUCCESS:
                jsonResult.setSuccess(true);
                break;

            case NOT_MATCH:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("令牌信息不存在");

            case ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("主动过期令牌出现错误");
                break;
        }
        return jsonResult;
    }

    /**
     * 刷新登录令牌
     *
     * @param request
     * @param refreshTokenSignature
     * @return
     */
    @RequestMapping(value = "/rest/token/refresh", method = RequestMethod.POST)
    @ResponseBody
    public TokenRefreshJsonResult RefreshToken(HttpServletRequest request
            , @RequestParam("token") String refreshTokenSignature) {
        TokenRefreshJsonResult result = new TokenRefreshJsonResult();
        //获取用户请求的IP地址
        String ip = ipService.GetRequestRealIPAddress(request);
        TokenRefreshResult tokenRefreshResult = loginTokenService
                .RefreshToken(refreshTokenSignature, ip);
        switch (tokenRefreshResult.getTokenValidResultEnum()) {
            case SUCCESS:
                //刷新令牌成功
                result.setSuccess(true);
                result.setAccessToken(new Token(tokenRefreshResult.getAccessToken()));
                result.setRefreshToken(new Token(tokenRefreshResult.getRefreshToken()));
                break;

            case ERROR:
                result.setSuccess(false);
                result.setMessage("刷新令牌服务暂不可用，请稍后再试");
                break;

            case UNUSUAL_LOCATION:
                result.setSuccess(false);
                result.setMessage("你正在非常用地点登录，请重新登录验证");
                break;

            case NOT_MATCH:
                result.setSuccess(false);
                result.setMessage("令牌信息不匹配");
                break;

            case EXPIRED:
                result.setSuccess(false);
                result.setMessage("你的登录凭证已过期，请重新登录验证");
                break;
        }
        return result;
    }

    /**
     * 用户登录接口
     *
     * @param request
     * @param user
     * @param unionId
     * @param method
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/userlogin", method = RequestMethod.POST)
    @ResponseBody
    public UserLoginJsonResult UserLogin(HttpServletRequest request, @Validated(value = UserLoginValidGroup.class) User user
            , @RequestParam("unionId") String unionId, @RequestParam(value = "method"
            , required = false, defaultValue = "0") LoginMethodEnum method, BindingResult bindingResult) {
        UserLoginJsonResult userLoginJsonResult = new UserLoginJsonResult();
        if (bindingResult.hasErrors()) {
            userLoginJsonResult.setSuccess(false);
            userLoginJsonResult.setMessage("请求参数不合法");
        } else {
            BaseResult<UserCertificate, LoginResultEnum> userLoginResult = null;
            switch (method) {
                case QUICK_LOGIN:
                    userLoginResult = userLoginService.UserLogin(request.getSession().getId(), user, true);
                    break;

                case CAS_LOGIN:
                    userLoginResult = userLoginService.UserLogin(request.getSession().getId(), user, false);
                    break;
            }
            switch (userLoginResult.getResultType()) {
                case PASSWORD_ERROR:
                    //用户名或密码错误
                    userLoginJsonResult.setSuccess(false);
                    userLoginJsonResult.setMessage("用户名或密码错误，登录失败");
                    break;

                case SERVER_ERROR:
                    //服务器异常
                    userLoginJsonResult.setSuccess(false);
                    userLoginJsonResult.setMessage("教务系统维护中，请稍候再试");
                    break;

                case TIME_OUT:
                    //连接超时
                    userLoginJsonResult.setSuccess(false);
                    userLoginJsonResult.setMessage("网络连接超时，请稍候再试");
                    break;

                case LOGIN_SUCCESS:
                    //登录成功
                    User resultUser = userLoginResult.getResultData().getUser();
                    try {
                        //同步数据库用户数据
                        userDataService.SyncUserData(resultUser);
                        //获取用户真实姓名
                        BaseResult<Profile, DataBaseResultEnum> getUserProfileResult = userProfileService
                                .GetUserProfile(user.getUsername());
                        resultUser.setRealname(Optional.ofNullable(getUserProfileResult.getResultData())
                                .map(Profile::getRealname).orElse(""));
                        //获取权限令牌和刷新令牌
                        BaseResult<AccessToken, TokenValidResultEnum> getAccessTokenResult = loginTokenService
                                .GetAccessToken(user.getUsername(), ipService.GetRequestRealIPAddress(request), unionId);
                        switch (getAccessTokenResult.getResultType()) {
                            case SUCCESS:
                                BaseResult<RefreshToken, TokenValidResultEnum> getRefreshTokenResult = loginTokenService
                                        .GetRefreshToken(getAccessTokenResult.getResultData());
                                switch (getRefreshTokenResult.getResultType()) {
                                    case SUCCESS:
                                        userLoginJsonResult.setSuccess(true);
                                        userLoginJsonResult.setUser(resultUser);
                                        userLoginJsonResult.setAccessToken(new Token(getAccessTokenResult.getResultData()));
                                        userLoginJsonResult.setRefreshToken(new Token(getRefreshTokenResult.getResultData()));
                                        break;

                                    case ERROR:
                                    default:
                                        userLoginJsonResult.setSuccess(false);
                                        userLoginJsonResult.setMessage("获取令牌信息失败，请稍候再试");
                                        break;
                                }
                                break;

                            case ERROR:
                            default:
                                userLoginJsonResult.setSuccess(false);
                                userLoginJsonResult.setMessage("获取令牌信息失败，请稍候再试");
                                break;
                        }
                    } catch (TransactionException e) {
                        userLoginJsonResult.setSuccess(false);
                        userLoginJsonResult.setMessage("学院系统维护中，请稍候再试");
                    }
                    break;
            }
        }
        return userLoginJsonResult;
    }

    /**
     * 用户登录
     *
     * @param request
     * @param response
     * @param redirectInfo
     * @param relink
     * @param user
     * @param bindingResult
     * @param redirectAttributes
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/userlogin")
    public ModelAndView UserLogin(HttpServletRequest request, HttpServletResponse response
            , RedirectInfo redirectInfo, boolean relink
            , @Validated(value = UserLoginValidGroup.class) User user
            , BindingResult bindingResult, RedirectAttributes redirectAttributes) throws WsgException {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("LoginErrorMessage", "请求参数异常");
            if (redirectInfo.needToRedirect()) {
                modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
            }
            modelAndView.setViewName("redirect:/login");
        } else {
            //清除已登录用户的用户凭证记录
            HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
            BaseResult<UserCertificate, LoginResultEnum> userLoginResult = userLoginService
                    .UserLogin(request.getSession().getId(), user, true);
            switch (userLoginResult.getResultType()) {
                case LOGIN_SUCCESS:
                    //登录成功
                    User resultUser = userLoginResult.getResultData().getUser();
                    //同步数据库用户数据
                    try {
                        userDataService.SyncUserData(resultUser);
                        //将用户信息数据写入Session
                        request.getSession().setAttribute("username", resultUser.getUsername());
                        request.getSession().setAttribute("password", resultUser.getPassword());
                        //同步教务系统会话
                        userLoginService.AsyncUpdateSession(request);
                        //将加密的用户信息保存到Cookie中
                        String username = StringEncryptUtils.encryptString(resultUser.getUsername());
                        String password = StringEncryptUtils.encryptString(resultUser.getPassword());
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
                        if (redirectInfo.needToRedirect()) {
                            modelAndView.setViewName("redirect:" + redirectInfo.getRedirect_url());
                        } else {
                            modelAndView.setViewName("redirect:/index");
                        }
                    } catch (TransactionException e) {
                        //同步数据失败
                        redirectAttributes.addFlashAttribute("LoginErrorMessage", "学院系统维护中，请稍候再试");
                        redirectAttributes.addFlashAttribute("LoginUsername", user.getUsername());
                        redirectAttributes.addFlashAttribute("LoginPassword", user.getPassword());
                        if (redirectInfo.needToRedirect()) {
                            modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
                        } else {
                            modelAndView.setViewName("redirect:/login");
                        }
                    }
                    break;

                case SERVER_ERROR:
                    //服务器异常
                    redirectAttributes.addFlashAttribute("LoginErrorMessage", "学院系统维护中，暂不可用");
                    redirectAttributes.addFlashAttribute("LoginUsername", user.getUsername());
                    redirectAttributes.addFlashAttribute("LoginPassword", user.getPassword());
                    if (redirectInfo.needToRedirect()) {
                        modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
                    }
                    modelAndView.setViewName("redirect:/login");
                    break;

                case TIME_OUT:
                    //连接超时
                    if (!relink) {
                        //如果第一次连接失败,则重新尝试一次
                        modelAndView.setViewName("forward:/userlogin?relink=true");
                    } else {
                        redirectAttributes.addFlashAttribute("LoginErrorMessage", "连接教务系统超时,请稍候再试");
                        redirectAttributes.addFlashAttribute("LoginUsername", user.getUsername());
                        redirectAttributes.addFlashAttribute("LoginPassword", user.getPassword());
                        if (redirectInfo.needToRedirect()) {
                            modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
                        }
                        modelAndView.setViewName("redirect:/login");
                    }
                    break;

                case PASSWORD_ERROR:
                    //密码错误
                    redirectAttributes.addFlashAttribute("LoginErrorMessage", "账号或密码错误，请检查并重试");
                    redirectAttributes.addFlashAttribute("LoginUsername", user.getUsername());
                    if (redirectInfo.needToRedirect()) {
                        modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
                    }
                    modelAndView.setViewName("redirect:/login");
                    break;
            }
        }
        return modelAndView;
    }

}
