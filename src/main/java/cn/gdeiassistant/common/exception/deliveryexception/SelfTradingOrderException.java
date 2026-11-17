package cn.gdeiassistant.common.exception.deliveryexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 当接单人和下单人为同一人时，抛出该异常
 */
public class SelfTradingOrderException extends KeyedBusinessException {

    public SelfTradingOrderException() {
    }

    public SelfTradingOrderException(String message) {
        super(message);
    }

    private SelfTradingOrderException(String key, Object[] args) {
        super(key, args, null);
    }

    public static SelfTradingOrderException ofKey(String key, Object... args) {
        return new SelfTradingOrderException(key, args);
    }
}
