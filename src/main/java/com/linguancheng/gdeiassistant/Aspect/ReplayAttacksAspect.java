package com.linguancheng.gdeiassistant.Aspect;

import com.linguancheng.gdeiassistant.Exception.RequestValidException.NonceInvalidException;
import com.linguancheng.gdeiassistant.Exception.RequestValidException.SignInvalidException;
import com.linguancheng.gdeiassistant.Exception.RequestValidException.TimestampInvalidException;
import com.linguancheng.gdeiassistant.Repository.Redis.Request.RequestDao;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
public class ReplayAttacksAspect {

    @Autowired
    private RequestDao requestDao;

    private String token;

    @Value("#{propertiesReader['request.validate.token']}")
    public void setToken(String token) {
        this.token = token;
    }

    @Pointcut("@annotation(com.linguancheng.gdeiassistant.Annotation.ReplayAttacksProtection)")
    public void LogicAction() {

    }

    @Before("LogicAction()")
    public void ValidateRequest(JoinPoint joinPoint) throws TimestampInvalidException, SignInvalidException, NonceInvalidException {
        Object[] args = joinPoint.getArgs();
        //获取请求的随机值和时间戳
        String nonce = (String) args[4];
        Long timestamp = (Long) args[5];
        String signature = (String) args[6];
        //进行摘要签名校验，保证数据未被篡改
        if (signature.equals(StringEncryptUtils.SHA1HexString(timestamp + nonce + token))) {
            //校验时间戳，请求时间戳与当前时间戳不超过60s
            if (Duration.between(Instant.ofEpochMilli(timestamp), Instant.now()).getSeconds() > 0
                    && Duration.between(Instant.now(), Instant.ofEpochMilli(timestamp)).getSeconds() <= 60) {
                if (StringUtils.isBlank(requestDao.QueryRequest(nonce))) {
                    //请求防重放攻击校验通过，保存随机值到缓存中
                    requestDao.InsertRequest(nonce, String.valueOf(timestamp));
                    return;
                }
                throw new NonceInvalidException("请求的随机值已经存在");
            }
            throw new TimestampInvalidException("时间戳校验不通过");
        }
        throw new SignInvalidException("请求摘要签名校验不通过");
    }
}