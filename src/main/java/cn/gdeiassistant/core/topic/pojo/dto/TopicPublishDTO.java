package cn.gdeiassistant.core.topic.pojo.dto;

import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 前端发布话题入参 DTO。
 */
public class TopicPublishDTO implements Serializable {

    @NotBlank(message = "话题关键词不能为空")
    @Length(min = 1, max = 15)
    private String topic;

    @NotBlank(message = "话题内容不能为空")
    @Length(min = 1, max = 250)
    private String content;

    @NotNull
    @Min(0)
    @Max(9)
    private Integer count;

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
