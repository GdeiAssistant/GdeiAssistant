package edu.gdei.gdeiassistant.Aspect;

import edu.gdei.gdeiassistant.Annotation.UserGroupAccess;
import edu.gdei.gdeiassistant.Enum.Base.DataBaseResultEnum;
import edu.gdei.gdeiassistant.Exception.UserAccessException.UserGroupNoAccessException;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.BaseResult;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class UserGroupAccessAspect {

    @Autowired
    private UserLoginService userLoginService;

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.UserGroupAccess)")
    public void RequestAction() {

    }

    @Before("RequestAction() && @annotation(userGroupAccess)")
    public void ValidateAdminAccess(JoinPoint joinPoint, UserGroupAccess userGroupAccess) throws UserGroupNoAccessException {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String username = (String) request.getSession().getAttribute("username");
        //查找数据库保存的用户信息
        BaseResult<User, DataBaseResultEnum> result = userLoginService.GetUserByUsername(username);
        if (result.getResultType() == DataBaseResultEnum.SUCCESS) {
            //对比用户组值是否与资源要求的权限值相匹配
            if (result.getResultData().getGroup().equals(userGroupAccess.group())) {
                return;
            }
        }
        throw new UserGroupNoAccessException("当前用户组无权限访问该资源");
    }
}
