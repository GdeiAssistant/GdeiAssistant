package com.linguancheng.gdeiassistant.Pojo.Result;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DataJsonResult<T> extends JsonResult {

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public DataJsonResult() {

    }

    public DataJsonResult(boolean success) {
        super(success);
    }

    public DataJsonResult(boolean success, T data) {
        super(success);
        this.data = data;
    }

    public DataJsonResult(boolean success, String message, T data) {
        super(success, message);
        this.data = data;
    }

    public DataJsonResult(Integer code, boolean success, String message, T data) {
        super(code, success, message);
        this.data = data;
    }
}
