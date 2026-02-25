package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import org.aspectj.lang.ProceedingJoinPoint;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 样板间模块处理器接口。
 * 每个 @TrialData(value = "...\") 模块可实现一个具体 Handler，负责测试账号下的业务行为。
 */
public interface TrialModuleHandler {

    /**
     * 模块标识，对应 @TrialData.value()。
     */
    String getModuleKey();

    /**
     * 处理测试账号请求。
     *
     * @param sessionId 当前会话 ID（由 JwtSessionIdFilter 注入）
     * @param request   当前 HttpServletRequest
     * @param trialData @TrialData 注解信息
     * @param joinPoint 原始 JoinPoint，可在需要时选择性调用 proceed()
     * @return 非 null 表示已处理并返回样板数据；null 表示不处理，交由后续逻辑
     */
    DataJsonResult<?> handle(String sessionId,
                             HttpServletRequest request,
                             TrialData trialData,
                             ProceedingJoinPoint joinPoint) throws Throwable;

    /**
     * 使用统一的 TrialErrorCode 构造错误返回值。
     */
    <T> DataJsonResult<T> buildError(TrialErrorCode errorCode);

    /**
     * 当前 Handler 支持的模块列表，默认仅包含 getModuleKey()。
     * 可在具体 Handler 中覆盖以支持多个 module。
     */
    default List<String> getSupportedModules() {
        String key = getModuleKey();
        return key == null ? Collections.emptyList() : Collections.singletonList(key);
    }
}

