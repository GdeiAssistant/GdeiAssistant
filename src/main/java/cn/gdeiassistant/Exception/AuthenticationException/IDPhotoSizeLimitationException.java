package cn.gdeiassistant.Exception.AuthenticationException;

/**
 * 证件照片的文件大小超过限制时抛出该异常
 */
public class IDPhotoSizeLimitationException extends Exception{

    public IDPhotoSizeLimitationException() {
    }

    public IDPhotoSizeLimitationException(String message) {
        super(message);
    }
}
