package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.trailData.service.TrialDataService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Supplier;

/**
 * 样板间处理器抽象基类：封装通用的 Trial 数据加载与兜底逻辑。
 */
public abstract class AbstractTrialModuleHandler implements TrialModuleHandler {

    @Autowired
    protected TrialDataService trialDataService;

    /**
     * 优先从 trial 集合加载样板数据；若不存在或失败，则执行 fallback。
     *
     * @param module   模块标识，对应 TrialDocument.type
     * @param fallback 当 trial 数据缺失或 success=false 时执行的兜底逻辑
     */
    protected DataJsonResult<?> loadFromTrialOrFallback(String module,
                                                        Supplier<DataJsonResult<?>> fallback) {
        DataJsonResult<?> raw = trialDataService.loadTrialData(module, false, null, 0);
        if (raw != null && raw.getData() != null && Boolean.TRUE.equals(raw.isSuccess())) {
            return raw;
        }
        return fallback.get();
    }

    /**
     * 默认实现：构造统一错误返回。
     */
    @Override
    public <T> DataJsonResult<T> buildError(TrialErrorCode errorCode) {
        JsonResult base = new JsonResult(errorCode.getCode(), false, errorCode.getMessage());
        return new DataJsonResult<>(base);
    }
}

