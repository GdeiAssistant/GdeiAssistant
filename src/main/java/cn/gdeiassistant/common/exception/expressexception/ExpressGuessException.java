package cn.gdeiassistant.common.exception.expressexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 表白墙模块猜一下功能异常基类
 */
public class ExpressGuessException extends KeyedBusinessException {

    public ExpressGuessException() {

    }

    public ExpressGuessException(String message) {
        super(message);
    }

    protected ExpressGuessException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }
}
