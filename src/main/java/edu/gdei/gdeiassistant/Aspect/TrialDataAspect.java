package edu.gdei.gdeiassistant.Aspect;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import edu.gdei.gdeiassistant.Annotation.TrialData;
import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Token.LoginTokenService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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
            DataJsonResult result = new Gson().fromJson(environment.getProperty("trial.data." + trialData.value())
                    , new TypeToken<DataJsonResult>() {
                    }.getType());
            //若请求方法具有时间信息的属性，将会填充到测试数据中
            if (StringUtils.isNotBlank(trialData.time())) {
                Object value = null;
                //获取方法的所有参数值和名称，拦截具有时间信息的属性字段
                String[] parameterNames = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterNames();
                for (int i = 0; i < parameterNames.length; i++) {
                    if (trialData.time().equals(parameterNames[i])) {
                        value = args[i];
                        //时间信息为空时，设置为默认值
                        if (value == null) {
                            Class clazz = ((MethodSignature) proceedingJoinPoint.getSignature())
                                    .getMethod().getParameterTypes()[i];
                            if (clazz.equals(Integer.class)) {
                                value = 1;
                            }
                        }
                    }
                }
                //将时间属性填充入测试数据中与TrialData注解中time参数的名称值相同的属性中
                ((LinkedTreeMap) result.getData()).put(trialData.time(), value);
            }
            return result;
        }
        if (trialData.base()) {
            return new DataJsonResult((JsonResult) proceedingJoinPoint.proceed(args));
        }
        return (DataJsonResult) proceedingJoinPoint.proceed(args);
    }
}
