package cn.gdeiassistant.Exception.AuthenticationException;

/**
 * 中国居民身份证校验信息不一致时抛出该异常
 */
public class InconsistentAuthenticationException extends Exception{

    public InconsistentAuthenticationException() {
    }

    public InconsistentAuthenticationException(String message) {
        super(message);
    }
}
