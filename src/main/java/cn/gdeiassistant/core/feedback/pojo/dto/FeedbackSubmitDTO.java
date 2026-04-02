package cn.gdeiassistant.core.feedback.pojo.dto;

import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 帮助与反馈 - 提交反馈的入参 DTO。
 */
public class FeedbackSubmitDTO implements Serializable {

    @NotBlank(message = "反馈内容不能为空")
    @Length(min = 1, max = 500)
    private String content;

    @Length(max = 100)
    private String contact;

    @Length(max = 50)
    private String type;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
