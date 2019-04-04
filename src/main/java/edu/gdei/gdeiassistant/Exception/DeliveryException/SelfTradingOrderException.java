package edu.gdei.gdeiassistant.Exception.DeliveryException;

/**
 * 当接单人和下单人为同一人时，抛出该异常
 */
public class SelfTradingOrderException extends Exception {

    public SelfTradingOrderException() {
    }

    public SelfTradingOrderException(String message) {
        super(message);
    }
}
