package edu.gdei.gdeiassistant.Exception.AuthenticationException;

/**
 * 上传的图片不包含身份证信息时，抛出该异常
 */
public class NonIDCardInfoException extends Exception {

    public NonIDCardInfoException() {
    }

    public NonIDCardInfoException(String message) {
        super(message);
    }
}
