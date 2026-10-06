package cn.gdeiassistant.common.pojo.result;


import java.io.Serializable;

public class JsonResult implements Serializable {

    private Integer code;

    private Boolean success;

    private String message;

    /** 稳定业务错误码，供客户端分支判断（如 AUTH_REQUIRED、PRIVACY_RESTRICTED） */
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String errorCode;

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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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
