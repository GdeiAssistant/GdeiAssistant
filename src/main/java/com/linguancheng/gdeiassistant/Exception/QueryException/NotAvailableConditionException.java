package com.gdeiassistant.gdeiassistant.Exception.QueryException;

/**
 * Created by gdeiassistant on 2017/7/24.
 */

/**
 * 查询条件暂不可用时抛出该异常
 */
public class NotAvailableConditionException extends ErrorQueryConditionException {

    public NotAvailableConditionException() {
        super();
    }

    public NotAvailableConditionException(String message) {
        super(message);
    }
}
