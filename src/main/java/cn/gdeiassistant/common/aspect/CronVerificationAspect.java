package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(6)
public class CronVerificationAspect {

    @Pointcut("execution(* cn.gdeiassistant.core.cron.controller.CronController.*(..))")
    public void CronAction() {

    }

    @Around("CronAction()")
    public JsonResult VerifyCronRequest(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String flag = request.getHeader("X-Appengine-Cron");
        if (StringUtils.isNotBlank(flag) && "true".equals(flag)) {
            return (JsonResult) proceedingJoinPoint.proceed(args);
        }
        return new JsonResult(false, "不合法的Cron请求");
    }
}
