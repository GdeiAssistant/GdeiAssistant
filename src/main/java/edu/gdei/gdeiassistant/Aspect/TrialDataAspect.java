package edu.gdei.gdeiassistant.Aspect;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.gdei.gdeiassistant.Annotation.TrialData;
import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class TrialDataAspect {

    @Autowired
    private Environment environment;

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.TrialData)")
    public void RequestAction() {

    }

    @Around("RequestAction() && @annotation(trialData)")
    public DataJsonResult CheckTrialUser(ProceedingJoinPoint proceedingJoinPoint, TrialData trialData) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        Integer group = (Integer) request.getSession().getAttribute("group");
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
