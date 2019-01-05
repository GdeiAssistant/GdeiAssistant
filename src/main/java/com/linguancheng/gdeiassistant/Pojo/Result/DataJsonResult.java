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

    public DataJsonResult(boolean success, T data) {
        super(success);
        this.data = data;
    }

    public DataJsonResult(JsonResult jsonResult) {
        if (jsonResult != null) {
            setCode(jsonResult.getCode());
            setSuccess(jsonResult.isSuccess());
            setMessage(jsonResult.getMessage());
        }
    }
}
