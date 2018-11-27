package com.linguancheng.gdeiassistant.Controller.UserLogin;

import com.linguancheng.gdeiassistant.Pojo.Redirect.RedirectInfo;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by linguancheng on 2017/7/16.
 */

@Controller
public class UserLoginController {

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

}
