package com.linguancheng.gdeiassistant.Interceptor;

import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by linguancheng on 2017/7/20.
 */
public class LoginInterceptor implements HandlerInterceptor {

    private List<String> exceptionList;

    public void setExceptionList(List<String> exceptionList) {
        this.exceptionList = exceptionList;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        //若用户访问的URL在例外列表中,则放行
        for (String string : exceptionList) {
            if (uri.startsWith(string) || uri.equals("/")) {
                return true;
            }
        }

        HttpSession httpSession = request.getSession();
        String username = (String) httpSession.getAttribute("username");
        String password = (String) httpSession.getAttribute("password");
        String keycode = (String) httpSession.getAttribute("keycode");
        String number = (String) httpSession.getAttribute("number");

        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password) && !StringUtils.isBlank(keycode)
                && !StringUtils.isBlank(number)) {
            return true;
        }

        //跳转到用户登录页面
        response.sendRedirect(request.getContextPath() + "/?redirect_url=" + uri);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
