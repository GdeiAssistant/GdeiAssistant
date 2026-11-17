package cn.gdeiassistant.common.exception.chargeexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 用户充值金额超过范围时抛出该异常
 */
public class AmountNotAvailableException extends KeyedBusinessException {

    public AmountNotAvailableException() {
        super();
    }

    public AmountNotAvailableException(String message) {
        super(message);
    }

    private AmountNotAvailableException(String key, Object[] args) {
        super(key, args, null);
    }

    public static AmountNotAvailableException ofKey(String key, Object... args) {
        return new AmountNotAvailableException(key, args);
    }
}
