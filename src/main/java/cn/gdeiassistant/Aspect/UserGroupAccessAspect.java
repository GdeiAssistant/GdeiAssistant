package cn.gdeiassistant.Aspect;

import cn.gdeiassistant.Annotation.UserGroupAccess;
import cn.gdeiassistant.Constant.ErrorConstantUtils;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Token.LoginTokenService;
import cn.gdeiassistant.Service.UserLogin.UserLoginService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class UserGroupAccessAspect {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private LoginTokenService loginTokenService;

    @Pointcut("@annotation(cn.gdeiassistant.Annotation.UserGroupAccess)")
    public void RequestAction() {

    }

    @Around("RequestAction() && @annotation(userGroupAccess)")
    public Object ValidateAdminAccess(ProceedingJoinPoint proceedingJoinPoint, UserGroupAccess userGroupAccess) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        User user = null;
        String username = (String) request.getSession().getAttribute("username");
        if (username == null) {
            //获取用户请求的权限令牌签名
            String token = request.getParameter("token");
            user = userLoginService.GetUserByUsername(loginTokenService.ParseToken(token).get("username").asString());
        } else {
            user = userLoginService.GetUserByUsername(username);
        }
        //对比用户组值是否与资源要求的权限值相匹配
        for (int group : userGroupAccess.group()) {
            if (user.getGroup().equals(group)) {
                if (userGroupAccess.rest()) {
                    return proceedingJoinPoint.proceed(args);
                }
                return new ModelAndView("Error/unauthorizedError");
            }
        }
        //当前用户组没有权限访问该资源或执行操作
        if (userGroupAccess.rest()) {
            return new JsonResult(ErrorConstantUtils.USER_GROUP_NO_ACCESS, false, "当前用户组没有权限访问该资源或执行操作");
        }
        return new ModelAndView("Error/unauthorizedError");
    }
}
