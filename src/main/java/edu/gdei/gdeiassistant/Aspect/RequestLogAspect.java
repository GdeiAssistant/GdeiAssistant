package edu.gdei.gdeiassistant.Aspect;

import com.alibaba.fastjson.JSON;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Aspect
@Component
@Order(3)
public class RequestLogAspect {

    private final Logger logger = LoggerFactory.getLogger(RequestLogAspect.class);

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.RequestLogPersistence)")
    public void RequestAction() {

    }

    @AfterReturning("RequestAction()")
    public void RestSaveQueryLog(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        HttpServletRequest request = (HttpServletRequest) args[0];
        StringBuilder stringBuilder = new StringBuilder("RequestLog - ");
        //记录请求的方法
        stringBuilder.append("RequestName:").append(joinPoint.getSignature().getDeclaringTypeName()).append(" - ").append(joinPoint.getSignature().getName());
        //记录请求时间
        stringBuilder.append(" . RequestTime:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        //记录请求头信息
        stringBuilder.append(" . RequestHeaders: ");
        stringBuilder.append(StringUtils.isBlank(request.getHeader("User-Agent")) ? "" : "User-Agent:" + request.getHeader("User-Agent") + " , ");
        stringBuilder.append(StringUtils.isBlank(request.getHeader("Client-Type")) ? "" : "Client-Type:" + request.getHeader("Client-Type") + " , ");
        stringBuilder.append(StringUtils.isBlank(request.getHeader("Version-Code")) ? "" : "Version-Code:" + request.getHeader(request.getHeader("Version-Code")));
        //记录非敏感请求参数信息
        stringBuilder.append(". RequestParameters: ");
        for (int i = 1; i < args.length; i++) {
            if (args[i] != null && !(parameterName[i].contains("password") || parameterName[i].contains("token"))) {
                if (i != 1) {
                    stringBuilder.append(" , ");
                }
                stringBuilder.append(parameterName[i])
                        .append(":").append(JSON.toJSONString(args[i]));
            }
        }
        stringBuilder.append(" . ");
        //保存请求信息到日志
        logger.info(stringBuilder.toString());
    }
}
