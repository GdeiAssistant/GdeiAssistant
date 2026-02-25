package cn.gdeiassistant.common.exception.CommonException;

/**
 * 功能未开启异常（特性开关 / 功能降级）。
 * 当第三方配置未配置或未启用时，业务层抛出此异常，由全局异常处理器统一返回 JSON。
 */
public class FeatureNotEnabledException extends RuntimeException {

    public FeatureNotEnabledException() {
        super("该功能未启用");
    }

    public FeatureNotEnabledException(String message) {
        super(message != null && !message.isEmpty() ? message : "该功能未启用");
    }
}
