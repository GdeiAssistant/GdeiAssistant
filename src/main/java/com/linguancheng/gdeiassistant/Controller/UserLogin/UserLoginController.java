package com.linguancheng.gdeiassistant.Controller.UserLogin;

import com.linguancheng.gdeiassistant.Pojo.Entity.*;
import com.linguancheng.gdeiassistant.Pojo.Redirect.RedirectInfo;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by linguancheng on 2017/7/16.
 */

@Controller
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

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
            , BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {
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
            UserCertificate userCertificate = userLoginService.UserLogin(request.getSession().getId(), user, true);
            //同步数据库用户数据
            userDataService.SyncUserData(userCertificate.getUser());
            //将用户信息数据写入Session
            request.getSession().setAttribute("username", userCertificate.getUser().getUsername());
            request.getSession().setAttribute("password", userCertificate.getUser().getPassword());
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
            if (redirectInfo.needToRedirect()) {
                modelAndView.setViewName("redirect:" + redirectInfo.getRedirect_url());
            } else {
                modelAndView.setViewName("redirect:/index");
            }
        }
        return modelAndView;
    }

}
