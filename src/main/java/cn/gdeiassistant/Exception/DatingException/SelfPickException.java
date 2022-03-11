package cn.gdeiassistant.Exception.DatingException;

/**
 * 不能向自己发布的卖室友信息发送撩一下请求，否则抛出该异常
 */
public class SelfPickException extends Exception{

    public SelfPickException() {
    }

    public SelfPickException(String message) {
        super(message);
    }
}
