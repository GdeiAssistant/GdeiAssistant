package cn.gdeiassistant.common.exception.authenticationexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

/**
 * 上传的证件照片超过限制数量时抛出该异常
 */
public class IDPhotoCountLimitationException extends KeyedBusinessException {

    public IDPhotoCountLimitationException() {
    }

    public IDPhotoCountLimitationException(String message) {
        super(message);
    }

    private IDPhotoCountLimitationException(String key, Object[] args) {
        super(key, args, null);
    }

    public static IDPhotoCountLimitationException ofKey(String key, Object... args) {
        return new IDPhotoCountLimitationException(key, args);
    }
}
