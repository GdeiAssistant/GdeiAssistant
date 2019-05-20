package edu.gdei.gdeiassistant.Aspect;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.gdei.gdeiassistant.Annotation.TrialData;
import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Token.LoginTokenService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(5)
public class TrialDataAspect {

    @Autowired
    private Environment environment;

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private UserLoginService userLoginService;

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.TrialData)")
    public void RequestAction() {

    }

    @Around("RequestAction() && @annotation(trialData)")
    public DataJsonResult CheckTrialUser(ProceedingJoinPoint proceedingJoinPoint, TrialData trialData) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        Integer group = (Integer) request.getSession().getAttribute("group");
        if (group == null) {
            //获取用户请求的权限令牌签名
            String token = (String) args[1];
            User user = userLoginService.GetUserByUsername(loginTokenService.ParseToken(token).get("username").asString());
            group = user.getGroup();
        }
        //若当前用户组为体验用户，则返回测试数据
        if (UserGroupEnum.TRIAL.getValue().equals(group)) {
            if (trialData.base()) {
                return new Gson().fromJson(environment.getProperty("trial.data." + trialData.value())
                        , new TypeToken<DataJsonResult>() {
                        }.getType());
            }
            return new Gson().fromJson(environment.getProperty("trial.data." + trialData.value())
                    , new TypeToken<DataJsonResult>() {
                    }.getType());
        } else {
            if (trialData.base()) {
                return new DataJsonResult((JsonResult) proceedingJoinPoint.proceed(args));
            }
            return (DataJsonResult) proceedingJoinPoint.proceed(args);
        }
    }
}
