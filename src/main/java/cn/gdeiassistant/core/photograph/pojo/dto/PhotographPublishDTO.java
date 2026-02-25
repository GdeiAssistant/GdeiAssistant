package cn.gdeiassistant.core.photograph.pojo.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 前端发布照片入参 DTO。
 */
public class PhotographPublishDTO implements Serializable {

    @NotBlank
    @Length(min = 1, max = 25)
    private String title;

    @Length(max = 50)
    private String content;

    @NotNull
    @Min(0)
    @Max(4)
    private Integer count;

    @NotNull
    @Min(1)
    @Max(3)
    private Integer type;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
}
