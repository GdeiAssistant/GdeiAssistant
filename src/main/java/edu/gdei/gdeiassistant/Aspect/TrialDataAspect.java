package edu.gdei.gdeiassistant.Aspect;

import edu.gdei.gdeiassistant.Annotation.TrialData;
import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Token.LoginTokenService;
import edu.gdei.gdeiassistant.Service.TrailData.TrialDataService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(5)
public class TrialDataAspect {

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private TrialDataService trialDataService;

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.TrialData)")
    public void RequestAction() {

    }

    @Around("RequestAction() && @annotation(trialData)")
    public DataJsonResult CheckTrialUser(ProceedingJoinPoint proceedingJoinPoint, TrialData trialData) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String username = (String) request.getSession().getAttribute("username");
        Integer group = (Integer) request.getSession().getAttribute("group");
        if (username == null || group == null) {
            //获取用户请求的权限令牌签名
            String token = request.getParameter("token");
            User user = userLoginService.GetUserByUsername(loginTokenService.ParseToken(token).get("username").asString());
            username = user.decryptUser().getUsername();
            group = user.getGroup();
        }
        //若当前用户组为体验用户，则返回模拟结果数据
        if (UserGroupEnum.TRIAL.getValue().equals(group)) {
            /*
              若TrialData注解的请求时间信息RequestTime值不为空字符串，则表示请求的数据要求携带时间信息
              将在请求参数中获取对应的属性值，该属性的名称与注解中RequestTime的值相同，将作为时间属性被用于获取模拟数据或模拟数据后加工的过程中
              当该属性值为空时，系统将设置为默认时间属性值
             */
            Object requestTimeValue = null;
            if (StringUtils.isNotBlank(trialData.requestTime())) {
                //获取方法的所有参数值和名称，拦截具有时间信息的属性字段
                String[] parameterNames = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterNames();
                for (int i = 0; i < parameterNames.length; i++) {
                    if (trialData.requestTime().equals(parameterNames[i])) {
                        requestTimeValue = args[i];
                        break;
                    }
                }
            }
            /*
              若TrialData注解的响应时间信息ResponseTime值不为空字符串，则表示返回的模拟结果数据返回值中包含时间属性
              将在请求参数中获取对应的属性值，该属性的名称与注解中ResponseTime的值相同，将作为时间属性被填充到测试数据中
              当该属性值为空时，系统将设置为默认时间属性值
             */
            Object responseTimeValue = null;
            if (StringUtils.isNotBlank(trialData.responseTime())) {
                //获取方法的所有参数值和名称，拦截具有时间信息的属性字段
                String[] parameterNames = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterNames();
                for (int i = 0; i < parameterNames.length; i++) {
                    if (trialData.responseTime().equals(parameterNames[i])) {
                        responseTimeValue = args[i];
                        break;
                    }
                }
            }
            //解析、加工并返回模拟结果数据
            return trialDataService.ParseTrialData(trialData.value(), trialData.base()
                    , trialData.requestTime(), requestTimeValue, trialData.responseTime()
                    , responseTimeValue, username);
        }
        //若非体验用户，则进行网络请求，返回真实数据
        if (trialData.base()) {
            return new DataJsonResult((JsonResult) proceedingJoinPoint.proceed(args));
        }
        return (DataJsonResult) proceedingJoinPoint.proceed(args);
    }
}
