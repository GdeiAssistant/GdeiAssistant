package com.gdeiassistant.gdeiassistant.Controller.Dispatcher;

import com.gdeiassistant.gdeiassistant.Enum.Base.LoginResultEnum;
import com.gdeiassistant.gdeiassistant.Exception.CommonException.SyncTransactionException;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.User;
import com.gdeiassistant.gdeiassistant.Pojo.Redirect.RedirectInfo;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseResult;
import com.gdeiassistant.gdeiassistant.Service.UserData.UserDataService;
import com.gdeiassistant.gdeiassistant.Tools.StringEncryptUtils;
import com.gdeiassistant.gdeiassistant.Service.UserLogin.AutoLoginService;
import com.gdeiassistant.gdeiassistant.Service.UserLogin.UserLoginService;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by gdeiassistant on 2017/7/17.
 */

@Controller
public class DispatcherController {

    private final int AUTOLOGIN_NOT = 0;
    private final int AUTOLOGIN_SESSION = 1;
    private final int AUTOLOGIN_COOKIE = 2;

    @Autowired
    private AutoLoginService autoLoginService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

    @RequestMapping(value = "/")
    public ModelAndView DoDispatch(HttpServletRequest request, HttpServletResponse response
            , boolean relink, RedirectInfo redirectInfo
            , RedirectAttributes redirectAttributes) throws WsgException {
        //�?查自动登录状�?
        ModelAndView modelAndView = new ModelAndView();
        int autoLoginState = autoLoginService.CheckAutoLogin(request);
        if (autoLoginState == AUTOLOGIN_NOT) {
            //不自动登�?,返回登录主页
            if (redirectInfo.needToRedirect()) {
                modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
            } else {
                modelAndView.setViewName("redirect:/login");
            }
        } else if (autoLoginState == AUTOLOGIN_COOKIE) {
            //获取Cookie中的用户信息自动登录
            Cookie cookies[] = request.getCookies();
            String cookieUsername = "";
            String cookiePassword = "";
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("username")) {
                    cookieUsername = cookie.getValue();
                }
                if (cookie.getName().equals("password")) {
                    cookiePassword = cookie.getValue();
                }
            }
            //将用户信息进行解�?
            cookieUsername = StringEncryptUtils.decryptString(cookieUsername);
            cookiePassword = StringEncryptUtils.decryptString(cookiePassword);
            //清除已登录用户的用户凭证记录
            userLoginService.ClearUserLoginCredentials(request);
            //进行用户登录
            BaseResult<User, LoginResultEnum> result = userLoginService.UserLogin(request
                    , new User(cookieUsername, cookiePassword), true);
            switch (result.getResultType()) {
                case LOGIN_SUCCESS:
                    //登录成功
                    User resultUser = result.getResultData();
                    //同步数据库用户数�?
                    try {
                        userDataService.SyncUserData(resultUser);
                        //将用户信息数据写入Session
                        request.getSession().setAttribute("username", resultUser.getUsername());
                        request.getSession().setAttribute("password", resultUser.getPassword());
                        request.getSession().setAttribute("keycode", resultUser.getKeycode());
                        request.getSession().setAttribute("number", resultUser.getNumber());
                        //将加密的用户信息保存到Cookie�?
                        String username = StringEncryptUtils.encryptString(resultUser.getUsername());
                        String password = StringEncryptUtils.encryptString(resultUser.getPassword());
                        Cookie usernameCookie = new Cookie("username", username);
                        Cookie passwordCookie = new Cookie("password", password);
                        //设置Cookie�?大有效时间为3个月
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
                    } catch (SyncTransactionException e) {
                        //同步数据失败
                        redirectAttributes.addFlashAttribute("LoginErrorMessage", "学院系统维护中，请稍候再�?");
                        redirectAttributes.addFlashAttribute("LoginUsername", resultUser.getUsername());
                        redirectAttributes.addFlashAttribute("LoginPassword", resultUser.getPassword());
                        if (redirectInfo.needToRedirect()) {
                            modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
                        } else {
                            modelAndView.setViewName("redirect:/login");
                        }
                    }
                    break;

                case SERVER_ERROR:
                    //服务器异�?
                    redirectAttributes.addFlashAttribute("LoginErrorMessage", "学院系统维护中，暂不可用");
                    redirectAttributes.addFlashAttribute("LoginUsername", cookieUsername);
                    redirectAttributes.addFlashAttribute("LoginPassword", cookiePassword);
                    if (redirectInfo.needToRedirect()) {
                        modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
                    }
                    modelAndView.setViewName("redirect:/login");
                    break;

                case TIME_OUT:
                    //连接超时
                    if (!relink) {
                        //如果第一次连接失�?,则重新尝试一�?
                        modelAndView.setViewName("forward:/?relink=true");
                    } else {
                        redirectAttributes.addFlashAttribute("LoginErrorMessage", "连接教务系统超时,请稍候再�?");
                        redirectAttributes.addFlashAttribute("LoginUsername", cookieUsername);
                        redirectAttributes.addFlashAttribute("LoginPassword", cookiePassword);
                        if (redirectInfo.needToRedirect()) {
                            modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
                        }
                        modelAndView.setViewName("redirect:/login");
                    }
                    break;

                case PASSWORD_ERROR:
                    //密码错误
                    redirectAttributes.addFlashAttribute("LoginErrorMessage", "账号或密码错误，请检查并重试");
                    redirectAttributes.addFlashAttribute("LoginUsername", cookieUsername);
                    if (redirectInfo.needToRedirect()) {
                        modelAndView.setViewName("redirect:/login?redirect_url=" + redirectInfo.getRedirect_url());
                    }
                    modelAndView.setViewName("redirect:/login");
                    break;
            }
        } else {
            if (redirectInfo.needToRedirect()) {
                modelAndView.setViewName("redirect:/" + redirectInfo.getRedirect_url());
            } else {
                modelAndView.setViewName("redirect:/index");
            }
        }
        return modelAndView;
    }
}
