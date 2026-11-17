package cn.gdeiassistant.common.exception.datingexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 不能向自己发布的卖室友信息发送撩一下请求，否则抛出该异常
 */
public class SelfPickException extends KeyedBusinessException {

    public SelfPickException() {
    }

    public SelfPickException(String message) {
        super(message);
    }

    private SelfPickException(String key, Object[] args) {
        super(key, args, null);
    }

    public static SelfPickException ofKey(String key, Object... args) {
        return new SelfPickException(key, args);
    }
}
