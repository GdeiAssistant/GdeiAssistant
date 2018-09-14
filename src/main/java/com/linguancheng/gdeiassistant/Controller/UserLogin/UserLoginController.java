package com.linguancheng.gdeiassistant.Controller.UserLogin;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.TransactionException;
import com.linguancheng.gdeiassistant.Pojo.Entity.Profile;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Redirect.RedirectInfo;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserLoginJsonResult;
import com.linguancheng.gdeiassistant.Service.Profile.UserProfileService;
import com.linguancheng.gdeiassistant.Service.UserData.UserDataService;
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
     * 用户登录接口
     *
     * @param request
     * @param user
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/userlogin", method = RequestMethod.POST)
    @ResponseBody
    public UserLoginJsonResult UserLogin(HttpServletRequest request, @Validated(value = UserLoginValidGroup.class) User user
            , Boolean quickLogin, BindingResult bindingResult) {
        UserLoginJsonResult userLoginJsonResult = new UserLoginJsonResult();
        if (bindingResult.hasErrors()) {
            userLoginJsonResult.setSuccess(false);
            userLoginJsonResult.setErrorMessage("请求参数不合法");
        } else {
            if (quickLogin == null) {
                quickLogin = true;
            }
            BaseResult<User, LoginResultEnum> userLoginResult = userLoginService.UserLogin(request, user, quickLogin);
            switch (userLoginResult.getResultType()) {
                case PASSWORD_ERROR:
                    //用户名或密码错误
                    userLoginJsonResult.setSuccess(false);
                    userLoginJsonResult.setErrorMessage("用户名或密码错误,登录失败");
                    break;

                case SERVER_ERROR:
                    //服务器异常
                    userLoginJsonResult.setSuccess(false);
                    userLoginJsonResult.setErrorMessage("教务系统维护中,请稍候再试");
                    break;

                case TIME_OUT:
                    //连接超时
                    userLoginJsonResult.setSuccess(false);
                    userLoginJsonResult.setErrorMessage("网络连接超时,请稍候再试");
                    break;

                case LOGIN_SUCCESS:
                    //登录成功
                    User resultUser = userLoginResult.getResultData();
                    try {
                        //同步数据库用户数据
                        userDataService.SyncUserData(resultUser);
                        //获取用户真实姓名
                        BaseResult<Profile, DataBaseResultEnum> getUserProfileResult = userProfileService
                                .GetUserProfile(user.getUsername());
                        userLoginJsonResult.setSuccess(true);
                        resultUser.setXm(Optional.ofNullable(getUserProfileResult.getResultData())
                                .map(Profile::getRealname).orElse(""));
                        userLoginJsonResult.setUser(resultUser);
                    } catch (TransactionException e) {
                        userLoginJsonResult.setSuccess(false);
                        userLoginJsonResult.setErrorMessage("学院系统维护中,请稍候再试");
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
            userLoginService.ClearUserLoginCredentials(request);
            BaseResult<User, LoginResultEnum> result = userLoginService.UserLogin(request, user, true);
            switch (result.getResultType()) {
                case LOGIN_SUCCESS:
                    //登录成功
                    User resultUser = result.getResultData();
                    //同步数据库用户数据
                    try {
                        userDataService.SyncUserData(resultUser);
                        //将用户信息数据写入Session
                        request.getSession().setAttribute("username", resultUser.getUsername());
                        request.getSession().setAttribute("password", resultUser.getPassword());
                        request.getSession().setAttribute("keycode", resultUser.getKeycode());
                        request.getSession().setAttribute("number", resultUser.getNumber());
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
