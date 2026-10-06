package cn.gdeiassistant.common.exception.databaseexception;


public class DataNotExistException extends Exception {

    public DataNotExistException() {
    }

    public DataNotExistException(String message) {
        super(message);
    }
}
