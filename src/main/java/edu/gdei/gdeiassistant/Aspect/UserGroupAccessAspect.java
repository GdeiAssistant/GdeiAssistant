package edu.gdei.gdeiassistant.Aspect;

import edu.gdei.gdeiassistant.Annotation.UserGroupAccess;
import edu.gdei.gdeiassistant.Constant.ErrorConstantUtils;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class UserGroupAccessAspect {

    @Autowired
    private UserLoginService userLoginService;

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.UserGroupAccess)")
    public void RequestAction() {

    }

    @Around("RequestAction() && @annotation(userGroupAccess)")
    public JsonResult ValidateAdminAccess(ProceedingJoinPoint proceedingJoinPoint, UserGroupAccess userGroupAccess) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String username = (String) request.getSession().getAttribute("username");
        User user = userLoginService.GetUserByUsername(username);
        //对比用户组值是否与资源要求的权限值相匹配
        for (int group : userGroupAccess.group()) {
            if (user.getGroup().equals(group)) {
                return (JsonResult) proceedingJoinPoint.proceed(args);
            }
        }
        //当前用户组没有权限访问该资源或执行操作
        return new JsonResult(ErrorConstantUtils.USER_GROUP_NO_ACCESS, false, "当前用户组没有权限访问该资源或执行操作");
    }
}
