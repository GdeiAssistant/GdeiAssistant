package cn.gdeiassistant.common.exception.deliveryexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 没有抢到快递代收订单时，抛出该异常
 */
public class DeliveryOrderTakenException extends KeyedBusinessException {

    public DeliveryOrderTakenException() {
    }

    public DeliveryOrderTakenException(String message) {
        super(message);
    }

    private DeliveryOrderTakenException(String key, Object[] args) {
        super(key, args, null);
    }

    public static DeliveryOrderTakenException ofKey(String key, Object... args) {
        return new DeliveryOrderTakenException(key, args);
    }
}
