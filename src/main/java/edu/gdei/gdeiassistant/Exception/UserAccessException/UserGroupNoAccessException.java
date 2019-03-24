package edu.gdei.gdeiassistant.Exception.UserAccessException;

/**
 * 用户所属组无权限访问时抛出该异常
 */
public class UserGroupNoAccessException extends Exception {

    public UserGroupNoAccessException() {

    }

    public UserGroupNoAccessException(String message) {
        super(message);
    }
}
