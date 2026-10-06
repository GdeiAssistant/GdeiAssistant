package cn.gdeiassistant.common.pojo.result;


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

    public DataJsonResult(boolean success){
        super(success);
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
