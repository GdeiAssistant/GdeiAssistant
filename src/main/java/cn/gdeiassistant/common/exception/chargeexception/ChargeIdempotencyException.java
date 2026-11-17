package cn.gdeiassistant.common.exception.chargeexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class ChargeIdempotencyException extends KeyedBusinessException {

    private final int code;

    public ChargeIdempotencyException(int code, String message) {
        super(message);
        this.code = code;
    }

    private ChargeIdempotencyException(int code, String key, Object[] args) {
        super(key, args, null);
        this.code = code;
    }

    public static ChargeIdempotencyException ofKey(int code, String key, Object... args) {
        return new ChargeIdempotencyException(code, key, args);
    }

    public int getCode() {
        return code;
    }
}
