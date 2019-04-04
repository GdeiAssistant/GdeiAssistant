package edu.gdei.gdeiassistant.Exception.DeliveryException;

/**
 * 没有抢到快递代收订单时，抛出该异常
 */
public class DeliveryOrderTakenException extends Exception {

    public DeliveryOrderTakenException() {
    }

    public DeliveryOrderTakenException(String message) {
        super(message);
    }
}
