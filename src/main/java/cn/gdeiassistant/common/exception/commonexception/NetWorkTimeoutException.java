package cn.gdeiassistant.common.exception.commonexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class NetWorkTimeoutException extends KeyedBusinessException {

    public NetWorkTimeoutException() {
        super();
    }

    public NetWorkTimeoutException(String message) {
        super(message);
    }

    public NetWorkTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    private NetWorkTimeoutException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }

    public static NetWorkTimeoutException ofKey(String key, Object... args) {
        return new NetWorkTimeoutException(key, args, null);
    }
}
