package com.linguancheng.gdeiassistant.Pojo.Result;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
public class JsonResult implements Serializable {

    private Integer code;

    private Boolean success;

    private String message;

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public JsonResult() {

    }

    public JsonResult(boolean success) {
        this.success = success;
    }

    public JsonResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public JsonResult(Integer code, boolean success, String message) {
        this.code = code;
        this.success = success;
        this.message = message;
    }
}
