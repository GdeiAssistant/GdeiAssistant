package edu.gdei.gdeiassistant.Exception.AuthenticationException;

/**
 * 上传的照片是其他类型的证件而非居民身份证，如军官证、武警警官证、士兵证、军队学员证等时，抛出该异常
 */
public class OtherTypeCardException extends Exception {

    public OtherTypeCardException() {
    }

    public OtherTypeCardException(String message) {
        super(message);
    }
}
