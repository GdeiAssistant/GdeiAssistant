package com.linguancheng.gdeiassistant.Controller.UserLogin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class LogoutController {

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void Logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //清除Session
        HttpSession httpSession = request.getSession();
        httpSession.invalidate();
        //清除Cookie
        Cookie cookies[] = request.getCookies();
        for (Cookie cookie : cookies) {
            cookie.setMaxAge(0);
            cookie.setPath(request.getContextPath() + "/");
            response.addCookie(cookie);
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}
