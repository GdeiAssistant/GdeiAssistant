package cn.gdeiassistant.core.express.pojo.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 前端发布表白时的入参 DTO。
 */
public class ExpressPublishDTO implements Serializable {

    @NotBlank(message = "昵称不能为空")
    @Length(min = 1, max = 10)
    private String nickname;

    @Length(max = 10)
    private String realname;

    @NotNull
    @Min(0)
    @Max(2)
    private Integer selfGender;

    @NotBlank(message = "被表白者名称不能为空")
    @Length(min = 1, max = 10)
    private String name;

    @NotBlank(message = "表白内容不能为空")
    @Length(min = 1, max = 250)
    private String content;

    @NotNull
    @Min(0)
    @Max(2)
    private Integer personGender;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRealname() { return realname; }
    public void setRealname(String realname) { this.realname = realname; }
    public Integer getSelfGender() { return selfGender; }
    public void setSelfGender(Integer selfGender) { this.selfGender = selfGender; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getPersonGender() { return personGender; }
    public void setPersonGender(Integer personGender) { this.personGender = personGender; }
}
