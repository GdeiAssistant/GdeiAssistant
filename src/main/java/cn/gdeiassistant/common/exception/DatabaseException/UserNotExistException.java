package cn.gdeiassistant.common.exception.DatabaseException;

/**
 * 查询的用户不存在时抛出该异常
 */
public class UserNotExistException extends DataNotExistException {

    public UserNotExistException() {

    }

    public UserNotExistException(String message) {
        super(message);
    }
}
