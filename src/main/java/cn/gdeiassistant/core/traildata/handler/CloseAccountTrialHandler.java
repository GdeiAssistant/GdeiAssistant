package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 账号注销模块样板间处理器：测试账号禁止真实注销。
 */
@Component
public class CloseAccountTrialHandler extends AbstractTrialModuleHandler {

    @Override
    public String getModuleKey() {
        return "closeAccount";
    }

    @Override
    public DataJsonResult<?> handle(String sessionId,
                                    HttpServletRequest request,
                                    TrialData trialData,
                                    ProceedingJoinPoint joinPoint) {
        // 测试账号仅返回失败提示，绝不进入真实删除逻辑
        return buildError(TrialErrorCode.CLOSE_ACCOUNT_DISABLED);
    }
}

