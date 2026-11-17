package cn.gdeiassistant.common.exception.recognitionexception;

import cn.gdeiassistant.common.exception.KeyedBusinessException;

public class RecognitionException extends KeyedBusinessException {

    public RecognitionException() {
        super();
    }

    public RecognitionException(String message) {
        super(message);
    }

    private RecognitionException(String key, Object[] args) {
        super(key, args, null);
    }

    public static RecognitionException ofKey(String key, Object... args) {
        return new RecognitionException(key, args);
    }
}
