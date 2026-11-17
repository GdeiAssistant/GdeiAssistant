package cn.gdeiassistant.common.exception.databaseexception;

/**
 * 查询的用户不存在时抛出该异常
 */
public class UserNotExistException extends DataNotExistException {

    public UserNotExistException() {
    }

    public UserNotExistException(String message) {
        super(message);
    }

    private UserNotExistException(String key, Object[] args) {
        super(key, args, null);
    }

    public static UserNotExistException ofKey(String key, Object... args) {
        return new UserNotExistException(key, args);
    }
}
