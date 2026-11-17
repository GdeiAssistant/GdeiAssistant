package cn.gdeiassistant.common.exception.authenticationexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 实名认证为非中国居民身份证时且未上传照片则抛出该异常
 */
public class NullIDPhotoException extends KeyedBusinessException {

    public NullIDPhotoException() {

    }

    public NullIDPhotoException(String message) {
        super(message);
    }

    private NullIDPhotoException(String key, Object[] args) {
        super(key, args, null);
    }

    public static NullIDPhotoException ofKey(String key, Object... args) {
        return new NullIDPhotoException(key, args);
    }
}
