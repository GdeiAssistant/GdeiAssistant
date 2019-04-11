package edu.gdei.gdeiassistant.Exception.DatabaseException;

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
