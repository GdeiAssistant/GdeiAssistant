package edu.gdei.gdeiassistant.Aspect;

import edu.gdei.gdeiassistant.Annotation.CheckAuthentication;
import edu.gdei.gdeiassistant.Annotation.RestCheckAuthentication;
import edu.gdei.gdeiassistant.Constant.ErrorConstantUtils;
import edu.gdei.gdeiassistant.Pojo.Entity.Authentication;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Authenticate.AuthenticateDataService;
import edu.gdei.gdeiassistant.Service.Token.LoginTokenService;
import edu.gdei.gdeiassistant.Tools.StringUtils;
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
public class AuthenticationAspect {

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private AuthenticateDataService authenticateDataService;

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.CheckAuthentication)")
    public void LogicAction() {

    }

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.RestCheckAuthentication)")
    public void RestLogicAction() {

    }

    @Around("LogicAction() && @annotation(checkAuthentication)")
    public ModelAndView CheckUserAuthentication(ProceedingJoinPoint proceedingJoinPoint
            , CheckAuthentication checkAuthentication) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String username = (String) request.getSession().getAttribute("username");
        Authentication authentication = authenticateDataService.QueryAuthenticationData(username);
        if (authentication != null) {
            //已经通过实名认证
            return (ModelAndView) proceedingJoinPoint.proceed(args);
        }
        //未完成实名认证，检查当前访问的单元模块是否要求实名认证
        if (Boolean.TRUE.equals(request.getServletContext().getAttribute("authentication."
                + checkAuthentication.name()))) {
            return new ModelAndView("Authenticate/tip");
        }
        return (ModelAndView) proceedingJoinPoint.proceed(args);
    }

    @Around("RestLogicAction() && @annotation(restCheckAuthentication)")
    public DataJsonResult RestCheckUserAuthentication(ProceedingJoinPoint proceedingJoinPoint
            , RestCheckAuthentication restCheckAuthentication) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String username = null;
        if (request.getRequestURI().startsWith("/rest")) {
            String token = request.getParameter("token");
            if (StringUtils.isNotBlank(token)) {
                username = loginTokenService.ParseToken(token).get("username").asString();
            }
        } else {
            username = (String) request.getSession().getAttribute("username");
        }
        Authentication authentication = authenticateDataService.QueryAuthenticationData(username);
        if (authentication != null) {
            //已经通过实名认证
            return (DataJsonResult) proceedingJoinPoint.proceed(args);
        }
        //未完成实名认证，检查当前访问的单元模块是否要求实名认证
        if (Boolean.TRUE.equals(request.getServletContext().getAttribute("authentication."
                + restCheckAuthentication.name()))) {
            return new DataJsonResult(new JsonResult(ErrorConstantUtils.NOT_AUTHENTICATION, false
                    , "请前往个人中心完成实名认证后再使用此功能"));
        }
        return (DataJsonResult) proceedingJoinPoint.proceed(args);
    }

}
