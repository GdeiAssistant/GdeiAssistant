package cn.gdeiassistant.Exception.AuthenticationException;

/**
 * 插入实名认证记录时已存在原有实名认证数据时抛出该异常，停止插入
 */
public class AuthenticationRecordExistException extends Exception{

    public AuthenticationRecordExistException() {
    }

    public AuthenticationRecordExistException(String message) {
        super(message);
    }
}
