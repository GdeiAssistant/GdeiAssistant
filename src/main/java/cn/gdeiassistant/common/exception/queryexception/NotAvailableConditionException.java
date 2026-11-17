package cn.gdeiassistant.common.exception.queryexception;

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

    private NotAvailableConditionException(String key, Object[] args) {
        super(key, args, null);
    }

    public static NotAvailableConditionException ofKey(String key, Object... args) {
        return new NotAvailableConditionException(key, args);
    }
}
