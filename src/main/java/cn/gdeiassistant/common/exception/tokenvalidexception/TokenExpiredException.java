package cn.gdeiassistant.common.exception.tokenvalidexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class TokenExpiredException extends KeyedBusinessException {

    public TokenExpiredException() {
        super();
    }

    public TokenExpiredException(String message) {
        super(message);
    }

    private TokenExpiredException(String key, Object[] args) {
        super(key, args, null);
    }

    public static TokenExpiredException ofKey(String key, Object... args) {
        return new TokenExpiredException(key, args);
    }
}
