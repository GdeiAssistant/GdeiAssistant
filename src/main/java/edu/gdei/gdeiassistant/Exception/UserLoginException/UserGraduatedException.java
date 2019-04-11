package edu.gdei.gdeiassistant.Exception.UserLoginException;

/**
 * 用户教务系统已毕业注销时抛出该异常
 */
public class UserGraduatedException extends Exception {

    public UserGraduatedException() {
    }

    public UserGraduatedException(String message) {
        super(message);
    }
}
