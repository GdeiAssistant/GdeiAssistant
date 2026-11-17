package cn.gdeiassistant.common.exception.authenticationexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 证件照片的文件大小超过限制时抛出该异常
 */
public class IDPhotoSizeLimitationException extends KeyedBusinessException {

    public IDPhotoSizeLimitationException() {
    }

    public IDPhotoSizeLimitationException(String message) {
        super(message);
    }

    private IDPhotoSizeLimitationException(String key, Object[] args) {
        super(key, args, null);
    }

    public static IDPhotoSizeLimitationException ofKey(String key, Object... args) {
        return new IDPhotoSizeLimitationException(key, args);
    }
}
