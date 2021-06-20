package cn.gdeiassistant.Aspect;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Service.IPAddress.IPService;
import cn.gdeiassistant.Service.Token.LoginTokenService;
import cn.gdeiassistant.Service.UserLogin.UserLoginService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class LoginTokenAspect {

    @Autowired
    private IPService ipService;

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private UserLoginService userLoginService;

    @Pointcut("@annotation(cn.gdeiassistant.Annotation.RestAuthentication)")
    public void QueryAction() {

    }

    @Before("QueryAction()")
    public void UserAuthenticate(JoinPoint joinPoint) throws Exception {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        //获取用户请求的权限令牌签名
        String token = request.getParameter("token");
        //获取用户请求的IP地址
        String ip = ipService.GetRequestRealIPAddress(request);
        //校验令牌信息
        loginTokenService.ValidToken(token, ip);
        //校验令牌通过
        User user = userLoginService.GetUserByUsername(loginTokenService.ParseToken(token).get("username").asString());
        request.setAttribute("user", user);
    }
}
