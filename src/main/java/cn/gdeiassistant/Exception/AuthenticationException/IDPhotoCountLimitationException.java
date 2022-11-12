package cn.gdeiassistant.Exception.AuthenticationException;

/**
 * 上传的证件照片超过限制数量时抛出该异常
 */
public class IDPhotoCountLimitationException extends Exception {

    public IDPhotoCountLimitationException() {
    }

    public IDPhotoCountLimitationException(String message) {
        super(message);
    }
}
