package cn.gdeiassistant.common.exception.deliveryexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 快递代收订单状态已更新时，抛出该异常
 */
public class DeliveryOrderStateUpdatedException extends KeyedBusinessException {

    public DeliveryOrderStateUpdatedException() {
    }

    public DeliveryOrderStateUpdatedException(String message) {
        super(message);
    }

    private DeliveryOrderStateUpdatedException(String key, Object[] args) {
        super(key, args, null);
    }

    public static DeliveryOrderStateUpdatedException ofKey(String key, Object... args) {
        return new DeliveryOrderStateUpdatedException(key, args);
    }
}
