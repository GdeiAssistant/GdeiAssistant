package cn.gdeiassistant.common.exception.CommonException;

/**
 * MongoDB 缓存清理失败时抛出（MySQL 主流程已成功）。
 * 用于部分成功 (Partial Success) 场景，由 Controller 捕获并返回 206。
 */
public class CacheClearException extends Exception {

    public CacheClearException(String message) {
        super(message);
    }

    public CacheClearException(String message, Throwable cause) {
        super(message, cause);
    }
}
