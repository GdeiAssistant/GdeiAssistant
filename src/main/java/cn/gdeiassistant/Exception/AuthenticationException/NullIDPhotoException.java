package cn.gdeiassistant.Exception.AuthenticationException;

/**
 * 实名认证为非中国居民身份证时且未上传照片则抛出该异常
 */
public class NullIDPhotoException extends Exception{

    public NullIDPhotoException() {

    }

    public NullIDPhotoException(String message) {
        super(message);
    }
}
