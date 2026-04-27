package cn.gdeiassistant.common.exception.ChargeException;

public class ChargeIdempotencyException extends Exception {

    private final int code;

    public ChargeIdempotencyException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
