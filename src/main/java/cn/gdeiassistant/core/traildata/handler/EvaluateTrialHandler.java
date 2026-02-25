package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * 一键评教样板间处理器：evaluate。
 */
@Component
public class EvaluateTrialHandler extends AbstractTrialModuleHandler {

    @Override
    public String getModuleKey() {
        return "evaluate";
    }

    @Override
    public DataJsonResult<?> handle(String sessionId,
                                    HttpServletRequest request,
                                    TrialData trialData,
                                    ProceedingJoinPoint joinPoint) {
        // 保持原有行为：返回 trial=true 的标记数据
        return new DataJsonResult<>(true, Collections.singletonMap("trial", true));
    }
}

