package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.config.properties.TrialProperties;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.trailData.handler.DefaultTrialHandler;
import cn.gdeiassistant.core.trailData.handler.TrialModuleHandler;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Aspect
@Component
@Order(5)
public class TrialDataAspect {

    @Autowired
    private TrialProperties trialProperties;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired(required = false)
    private List<TrialModuleHandler> trialModuleHandlers;

    private Map<String, TrialModuleHandler> handlerMap = Collections.emptyMap();

    private TrialModuleHandler defaultHandler;

    @PostConstruct
    public void initHandlers() {
        if (trialModuleHandlers == null || trialModuleHandlers.isEmpty()) {
            handlerMap = Collections.emptyMap();
            defaultHandler = null;
            return;
        }
        Map<String, TrialModuleHandler> map = new HashMap<>();
        for (TrialModuleHandler handler : trialModuleHandlers) {
            for (String key : handler.getSupportedModules()) {
                if (key == null) {
                    continue;
                }
                if (DefaultTrialHandler.DEFAULT_KEY.equals(key)) {
                    defaultHandler = handler;
                } else {
                    map.put(key, handler);
                }
            }
        }
        handlerMap = Collections.unmodifiableMap(map);
    }

    @Pointcut("@annotation(cn.gdeiassistant.common.annotation.TrialData)")
    public void RequestAction() {

    }

    @Around("RequestAction() && @annotation(trialData)")
    public DataJsonResult CheckTrialUser(ProceedingJoinPoint proceedingJoinPoint, TrialData trialData) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];

        // 优先使用 JwtSessionIdFilter 已注入的 sessionId（统一鉴权，仅接受 Authorization: Bearer）
        String sessionId = (String) request.getAttribute("sessionId");
        if (StringUtils.isBlank(sessionId)) {
            return buildError(TrialErrorCode.INVALID_TOKEN);
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            // 会话已在服务端丢失（如重启导致 Redis 清空），直接视为无效令牌，避免继续执行真实业务抛 NPE
            return buildError(TrialErrorCode.INVALID_TOKEN);
        }

        if (isTestAccount(user.getUsername())) {
            String module = trialData.value();
            TrialModuleHandler handler = handlerMap.get(module);
            if (handler == null) {
                handler = defaultHandler;
            }
            if (handler != null) {
                DataJsonResult<?> result = handler.handle(sessionId, request, trialData, proceedingJoinPoint);
                if (result != null) {
                    return result;
                }
            }
            // 测试账号命中样板间但未返回任何结果时，一律按“不支持该功能”兜底，绝不穿透到真实业务
            return buildError(TrialErrorCode.NOT_SUPPORTED);
        }

        if (trialData.base()) {
            return new DataJsonResult((JsonResult) proceedingJoinPoint.proceed(args));
        }
        return (DataJsonResult) proceedingJoinPoint.proceed(args);
    }

    private boolean isTestAccount(String username) {
        if (username == null) {
            return false;
        }
        List<String> testAccounts = trialProperties.getTestAccounts();
        if (testAccounts == null) {
            return false;
        }
        return testAccounts.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(s -> s.equalsIgnoreCase(username));
    }

    private DataJsonResult<?> buildError(TrialErrorCode errorCode) {
        JsonResult base = new JsonResult(errorCode.getCode(), false, errorCode.getMessage());
        return new DataJsonResult<>(base);
    }
}
