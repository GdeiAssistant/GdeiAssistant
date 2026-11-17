package cn.gdeiassistant.common.exception.closeaccountexception;

/**
 * 用户账号状态异常时抛出该异常
 */
public class UserStateErrorException extends CloseAccountException {

    public UserStateErrorException() {
    }

    public UserStateErrorException(String message) {
        super(message);
    }

    private UserStateErrorException(String key, Object[] args) {
        super(key, args, null);
    }

    public static UserStateErrorException ofKey(String key, Object... args) {
        return new UserStateErrorException(key, args);
    }
}
