package cn.gdeiassistant.core.secret.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 前端发布树洞入参 DTO。
 */
public class SecretPublishDTO implements Serializable {

    @NotNull(message = "主题不能为空")
    @Min(1)
    @Max(12)
    private Integer theme;

    @NotBlank(message = "树洞内容不能为空")
    @Size(min = 1, max = 100)
    private String content;

    @NotNull(message = "类型不能为空")
    @Min(0)
    @Max(2)
    private Integer type;

    @Min(0)
    @Max(1)
    private Integer timer;

    public Integer getTheme() { return theme; }
    public void setTheme(Integer theme) { this.theme = theme; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getTimer() { return timer; }
    public void setTimer(Integer timer) { this.timer = timer; }
}
