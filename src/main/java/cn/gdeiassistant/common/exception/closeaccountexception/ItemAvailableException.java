package cn.gdeiassistant.common.exception.closeaccountexception;

/**
 * 关闭账号时，用户有未处理的社区功能信息时，抛出该异常
 */
public class ItemAvailableException extends CloseAccountException {

    public ItemAvailableException() {
    }

    public ItemAvailableException(String message) {
        super(message);
    }

    private ItemAvailableException(String key, Object[] args) {
        super(key, args, null);
    }

    public static ItemAvailableException ofKey(String key, Object... args) {
        return new ItemAvailableException(key, args);
    }
}
