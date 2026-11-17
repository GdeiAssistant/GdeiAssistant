package cn.gdeiassistant.common.exception.bookrenewexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 图书续借超过次数限制时抛出该异常
 */
public class BookRenewOvertimeException extends KeyedBusinessException {

    public BookRenewOvertimeException() {

    }

    public BookRenewOvertimeException(String message) {
        super(message);
    }

    public BookRenewOvertimeException(String message, Throwable cause) {
        super(message, cause);
    }

    private BookRenewOvertimeException(String key, Object[] args) {
        super(key, args, null);
    }

    public static BookRenewOvertimeException ofKey(String key, Object... args) {
        return new BookRenewOvertimeException(key, args);
    }
}
