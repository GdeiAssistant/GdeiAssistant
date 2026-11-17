package cn.gdeiassistant.common.exception.authenticationexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 插入实名认证记录时已存在原有实名认证数据时抛出该异常，停止插入
 */
public class AuthenticationRecordExistException extends KeyedBusinessException {

    public AuthenticationRecordExistException() {
    }

    public AuthenticationRecordExistException(String message) {
        super(message);
    }

    private AuthenticationRecordExistException(String key, Object[] args) {
        super(key, args, null);
    }

    public static AuthenticationRecordExistException ofKey(String key, Object... args) {
        return new AuthenticationRecordExistException(key, args);
    }
}
