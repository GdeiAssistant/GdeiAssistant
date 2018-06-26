package com.linguancheng.gdeiassistant.Exception.QueryException;

/**
 * Created by linguancheng on 2017/7/21.
 */

/**
 * 查询条件异常类的基类
 */
public class ErrorQueryConditionException extends Exception {

    public ErrorQueryConditionException() {
        super();
    }

    public ErrorQueryConditionException(String message) {
        super(message);
    }
}
