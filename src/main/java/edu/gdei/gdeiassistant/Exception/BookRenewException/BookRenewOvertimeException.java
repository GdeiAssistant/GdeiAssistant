package edu.gdei.gdeiassistant.Exception.BookRenewException;

/**
 * 图书续借超过次数限制时抛出该异常
 */
public class BookRenewOvertimeException extends Exception {

    public BookRenewOvertimeException() {

    }

    public BookRenewOvertimeException(String message) {
        super(message);
    }
}
