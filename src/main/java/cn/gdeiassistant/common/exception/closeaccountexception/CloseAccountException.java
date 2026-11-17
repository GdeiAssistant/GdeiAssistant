package cn.gdeiassistant.common.exception.closeaccountexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public abstract class CloseAccountException extends KeyedBusinessException {

    public CloseAccountException() {
    }

    public CloseAccountException(String message) {
        super(message);
    }

    protected CloseAccountException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }
}
