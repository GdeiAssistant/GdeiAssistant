package edu.gdei.gdeiassistant.Interceptor;

import com.google.gson.Gson;
import edu.gdei.gdeiassistant.Constant.ErrorConstantUtils;
import edu.gdei.gdeiassistant.Pojo.Entity.Authentication;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Authenticate.AuthenticateDataService;
import edu.gdei.gdeiassistant.Service.Token.LoginTokenService;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthenticateDataService authenticateDataService;

    @Autowired
    private LoginTokenService loginTokenService;

    private List<String> exceptionList;

    public AuthenticationInterceptor() {

    }

    public AuthenticationInterceptor(List<String> exceptionList) {
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
        String username = null;
        if (uri.startsWith("/rest")) {
            String token = request.getParameter("token");
            if (StringUtils.isNotBlank(token)) {
                username = loginTokenService.ParseToken(token).get("username").asString();
            }
        } else {
            username = (String) request.getSession().getAttribute("username");
        }
        if (StringUtils.isBlank(username)) {
            return true;
        }
        Authentication authentication = authenticateDataService.QueryAuthenticationData(username);
        if (authentication != null) {
            //已经通过实名认证
            return true;
        } else {
            //未通过实名认证
            if (request.getServletContext().getAttribute("authentication.force").equals(true)) {
                //强制实名认证，跳转到实名认证提示页面或返回错误信息
                //数据接口拒绝操作和访问资源，返回JSON提示信息
                if (uri.startsWith("/api") || uri.startsWith("/rest")) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().print(new Gson().toJson(new JsonResult(ErrorConstantUtils.NOT_AUTHENTICATION
                            , false, "未完成实名认证，请首先使用广东二师助手网页版进行实名认证")));
                    response.getWriter().flush();
                    response.getWriter().close();
                    return false;
                }
                //其他URL跳转到实名认证提示页面
                response.sendRedirect(request.getContextPath() + "/authentication/tip");
                return false;
            }
            //不启用强制实名认证，不进行拦截
            return true;
        }

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
