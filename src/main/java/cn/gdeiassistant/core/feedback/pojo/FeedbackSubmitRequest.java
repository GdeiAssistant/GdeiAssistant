package cn.gdeiassistant.core.feedback.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 帮助与反馈 - 提交反馈的 JSON 请求体
 */
@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedbackSubmitRequest implements Serializable {

    @NotBlank(message = "反馈内容不能为空")
    @Length(min = 1, max = 500)
    private String content;

    @Length(max = 100)
    private String contact;

    @Length(max = 50)
    private String type;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
