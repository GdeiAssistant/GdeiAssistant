package edu.gdei.gdeiassistant.Aspect;

import edu.gdei.gdeiassistant.Exception.RequestValidException.NonceInvalidException;
import edu.gdei.gdeiassistant.Exception.RequestValidException.SignInvalidException;
import edu.gdei.gdeiassistant.Exception.RequestValidException.TimestampInvalidException;
import edu.gdei.gdeiassistant.Pojo.Entity.RequestValidation;
import edu.gdei.gdeiassistant.Repository.Redis.Request.RequestDao;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@Order(0)
public class ReplayAttacksAspect {

    @Autowired
    private RequestDao requestDao;

    private String token;

    @Value("#{propertiesReader['request.validate.token']}")
    public void setToken(String token) {
        this.token = token;
    }

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.ReplayAttacksProtection)")
    public void LogicAction() {

    }

    @Before("LogicAction()")
    public void ValidateRequest(JoinPoint joinPoint) throws TimestampInvalidException, SignInvalidException, NonceInvalidException {
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取请求的随机值、时间戳和签名
        RequestValidation requestValidation = null;
        for (int i = 1; i < methodSignature.getParameterNames().length; i++) {
            if (methodSignature.getParameterNames()[i].equals("requestValidation")) {
                requestValidation = (RequestValidation) args[i];
            }
        }
        //进行摘要签名校验，保证数据未被篡改
        if (requestValidation != null && StringUtils.isNotBlank(requestValidation.getSignature())
                && requestValidation.getSignature().equals(StringEncryptUtils.SHA1HexString(requestValidation.getTimestamp()
                + requestValidation.getNonce() + token))) {
            //校验时间戳，请求时间戳与当前时间戳不超过60s
            if (requestValidation.getTimestamp() != null && Duration.between(Instant.ofEpochMilli(requestValidation.getTimestamp()), Instant.now()).getSeconds() >= -5
                    && Duration.between(Instant.now(), Instant.ofEpochMilli(requestValidation.getTimestamp())).getSeconds() <= 60) {
                if (StringUtils.isNotBlank(requestValidation.getNonce()) && StringUtils.isBlank(requestDao.QueryRequest(requestValidation.getNonce()))) {
                    //请求防重放攻击校验通过，保存随机值到缓存中
                    requestDao.InsertRequest(requestValidation.getNonce(), String.valueOf(requestValidation.getTimestamp()));
                    return;
                }
                throw new NonceInvalidException("请求的随机值已经存在");
            }
            throw new TimestampInvalidException("时间戳校验不通过");
        }
        throw new SignInvalidException("请求摘要签名校验不通过");
    }
}