package edu.gdei.gdeiassistant.Exception.DeliveryException;

/**
 * 快递代收订单状态已更新时，抛出该异常
 */
public class DeliveryOrderStateUpdatedException extends Exception {

    public DeliveryOrderStateUpdatedException() {
    }

    public DeliveryOrderStateUpdatedException(String message) {
        super(message);
    }
}
