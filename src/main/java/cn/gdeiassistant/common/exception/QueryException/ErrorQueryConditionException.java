package cn.gdeiassistant.common.exception.QueryException;

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
