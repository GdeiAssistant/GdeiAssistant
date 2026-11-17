package cn.gdeiassistant.common.exception.queryexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 查询条件异常类的基类
 */
public class ErrorQueryConditionException extends KeyedBusinessException {

    public ErrorQueryConditionException() {
        super();
    }

    public ErrorQueryConditionException(String message) {
        super(message);
    }

    protected ErrorQueryConditionException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }

    public static ErrorQueryConditionException ofKey(String key, Object... args) {
        return new ErrorQueryConditionException(key, args, null);
    }
}
