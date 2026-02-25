package cn.gdeiassistant.core.roommate.pojo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;

/**
 * 发布/更新卖室友信息入参 DTO。
 */
public class RoommatePublishDTO implements Serializable {

    @Length(min = 1, max = 15)
    private String nickname;

    @Min(1)
    @Max(4)
    private Integer grade;

    @Length(min = 1, max = 12)
    private String faculty;

    @Length(min = 1, max = 10)
    private String hometown;

    @Length(min = 1, max = 100)
    private String content;

    @Length(min = 1, max = 15)
    private String qq;

    @Length(min = 1, max = 20)
    private String wechat;

    @Min(0)
    @Max(1)
    private Integer area;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public String getHometown() { return hometown; }
    public void setHometown(String hometown) { this.hometown = hometown; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public String getWechat() { return wechat; }
    public void setWechat(String wechat) { this.wechat = wechat; }
    public Integer getArea() { return area; }
    public void setArea(Integer area) { this.area = area; }
}
