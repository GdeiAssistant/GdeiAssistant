package cn.gdeiassistant.Aspect;

import cn.gdeiassistant.Annotation.TrialData;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AcademicAffairs.TrailData.TrialDataService;
import cn.gdeiassistant.Service.AccountManagement.Token.LoginTokenService;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(5)
@PropertySource("classpath:/config/trial/setting.properties")
public class TrialDataAspect implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private TrialDataService trialDataService;

    @Pointcut("@annotation(cn.gdeiassistant.Annotation.TrialData)")
    public void RequestAction() {

    }

    @Around("RequestAction() && @annotation(trialData)")
    public DataJsonResult CheckTrialUser(ProceedingJoinPoint proceedingJoinPoint, TrialData trialData) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String sessionId = null;
        if (trialData.rest()) {
            //令牌资源访问
            String token = request.getParameter("token");
            sessionId = loginTokenService.ParseToken(token).get("sessionId").asString();
        }
        //API访问
        sessionId = request.getSession().getId();

        //若设置模拟教务查询数据，则返回模拟数据
        if (BooleanUtils.isTrue(Boolean.valueOf(environment.getProperty("trial.data.simulation")))) {
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
            return trialDataService.ParseTrialData(sessionId, trialData.value(), trialData.base()
                    , trialData.requestTime(), requestTimeValue, trialData.responseTime()
                    , responseTimeValue);
        }
        //若非体验用户，则进行网络请求，返回真实数据
        if (trialData.base()) {
            return new DataJsonResult((JsonResult) proceedingJoinPoint.proceed(args));
        }
        return (DataJsonResult) proceedingJoinPoint.proceed(args);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
