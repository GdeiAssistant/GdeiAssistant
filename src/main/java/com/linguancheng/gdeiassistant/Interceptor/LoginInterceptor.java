package com.linguancheng.gdeiassistant.Interceptor;

import com.google.gson.Gson;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
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

        //校验用户登录状态
        HttpSession httpSession = request.getSession();
        String username = (String) httpSession.getAttribute("username");
        String password = (String) httpSession.getAttribute("password");

        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
            return true;
        }

        //若用户访问的URL是API接口，校验不通过则返回JSON字符串
        if (uri.startsWith("/api")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(new Gson().toJson(new JsonResult(false, "用户登录凭证过期，请重新登录")));
            response.getWriter().flush();
            response.getWriter().close();
            return false;
        }

        //若是普通页面，校验不通过则返回到登录页面
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
