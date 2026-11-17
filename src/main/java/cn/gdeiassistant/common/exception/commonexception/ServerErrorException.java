package cn.gdeiassistant.common.exception.commonexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class ServerErrorException extends KeyedBusinessException {

    public ServerErrorException() {
        super();
    }

    public ServerErrorException(String message) {
        super(message);
    }

    public ServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    private ServerErrorException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }

    public static ServerErrorException ofKey(String key, Object... args) {
        return new ServerErrorException(key, args, null);
    }

    public static ServerErrorException ofKey(String key, Throwable cause, Object... args) {
        return new ServerErrorException(key, args, cause);
    }
}
