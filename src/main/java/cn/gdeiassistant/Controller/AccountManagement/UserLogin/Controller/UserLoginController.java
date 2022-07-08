package cn.gdeiassistant.Controller.AccountManagement.UserLogin.Controller;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.Redirect.RedirectInfo;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.AutoLoginService;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserLoginService;
import cn.gdeiassistant.Tools.Utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class UserLoginController {

    private final int AUTOLOGIN_NOT = 0;

    private final int AUTOLOGIN_SESSION = 1;

    private final int AUTOLOGIN_COOKIE = 2;

    @Autowired
    private AutoLoginService autoLoginService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserCertificateService userCertificateService;

    /**
     * 用户登录，若自动登录失败，则进入登录界面
     *
     * @param request
     * @param response
     * @param redirectInfo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login")
    public ModelAndView DoDispatch(HttpServletRequest request, HttpServletResponse response
            , RedirectInfo redirectInfo) throws Exception {
        //检查自动登录状态
        ModelAndView modelAndView = new ModelAndView();
        int autoLoginState = autoLoginService.CheckAutoLogin(request);
        if (autoLoginState == AUTOLOGIN_NOT) {
            //不自动登录,返回登录主页
            if (redirectInfo.needToRedirect()) {
                modelAndView.addObject("RedirectURL", redirectInfo.getRedirect_url());
            }
            modelAndView.setViewName("Login/login");
        } else if (autoLoginState == AUTOLOGIN_COOKIE) {
            //获取Cookie中的用户信息自动登录
            Cookie[] cookies = request.getCookies();
            String cookieId = "";
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("cookieId")) {
                    cookieId = cookie.getValue();
                }
            }
            //清除已登录用户的用户凭证记录
            HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
            //获取用户Cookie凭证
            User user = userCertificateService.GetUserCookieCertificate(cookieId);
            //进行用户登录
            userLoginService.UserLogin(request.getSession().getId(), user.getUsername(), user.getPassword());
            //异步地与教务系统会话进行同步
            userCertificateService.AsyncUpdateSessionCertificate(request.getSession().getId(), user);
            //更新Cookie凭证有效期
            userCertificateService.UpdateUserCookieExpiration(cookieId);
            //更新Cookie有效期
            Cookie cookie = new Cookie("cookieId", cookieId);
            //设置Cookie最大有效时间为1个月
            cookie.setMaxAge(2592000);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            if (redirectInfo.needToRedirect()) {
                modelAndView.setViewName("redirect:" + redirectInfo.getRedirect_url());
            } else {
                modelAndView.setViewName("redirect:/index");
            }
        } else {
            if (redirectInfo.needToRedirect()) {
                modelAndView.addObject("RedirectURL", redirectInfo.getRedirect_url());
                modelAndView.setViewName("Login/login");
            } else {
                modelAndView.setViewName("redirect:/index");
            }
        }
        return modelAndView;
    }

}
