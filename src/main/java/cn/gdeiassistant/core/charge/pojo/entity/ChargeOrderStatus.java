package cn.gdeiassistant.core.charge.pojo.entity;

/**
 * Charge order lifecycle states.
 *
 * PAYMENT_SESSION_CREATED only means the external payment session/link was
 * generated. It does not mean the campus card balance has been settled.
 */
public enum ChargeOrderStatus {

    CREATED,
    PROCESSING,
    PAYMENT_SESSION_CREATED,
    FAILED,
    UNKNOWN,
    MANUAL_REVIEW
}
