package cn.gdeiassistant.core.feedback.pojo.vo;

import java.io.Serializable;

/**
 * 提交反馈成功后的视图（可选，当前仅返回 JsonResult）。
 */
public class FeedbackSubmitVO implements Serializable {

    private boolean success;
    private String message;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
