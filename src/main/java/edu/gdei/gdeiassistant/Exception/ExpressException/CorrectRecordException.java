package edu.gdei.gdeiassistant.Exception.ExpressException;

/**
 * 当已经存在正确的猜一下记录时，不允许再写入新的猜一下记录，抛出该异常
 */
public class CorrectRecordException extends ExpressGuessException {

    public CorrectRecordException() {

    }

    public CorrectRecordException(String message) {
        super(message);
    }
}
