package cn.gdeiassistant.common.exception.databaseexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class DataNotExistException extends KeyedBusinessException {

    public DataNotExistException() {
    }

    public DataNotExistException(String message) {
        super(message);
    }

    protected DataNotExistException(String key, Object[] args, Throwable cause) {
        super(key, args, cause);
    }

    public static DataNotExistException ofKey(String key, Object... args) {
        return new DataNotExistException(key, args, null);
    }
}
