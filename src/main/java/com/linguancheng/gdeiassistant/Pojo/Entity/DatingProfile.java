package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatingProfile implements Serializable {

    /**
     * 卖室友信息主键ID
     */
    private Integer profileId;

    /**
     * 发布者用户名
     */
    private String username;

    /**
     * 昵称
     */
    @Length(min = 1, max = 15)
    private String kickname;

    /**
     * 年级，由1-4分别代表大一至大四
     */
    @Min(1)
    @Max(4)
    private Integer grade;

    /**
     * 专业
     */
    @Length(min = 1, max = 12)
    private String faculty;

    /**
     * 家乡
     */
    @Length(min = 1, max = 10)
    private String hometown;

    /**
     * 条件
     */
    @Length(min = 1, max = 100)
    private String content;

    /**
     * QQ号码
     */
    @Length(min = 1, max = 15)
    private String qq;

    /**
     * 微信号
     */
    @Length(min = 1, max = 20)
    private String wechat;

    /**
     * 信息所属区域
     */
    @Min(0)
    @Max(1)
    private Integer area;

    /**
     * 信息状态，0为不可见，1可被其他用户看见
     */
    private Integer state;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKickname() {
        return kickname;
    }

    public void setKickname(String kickname) {
        this.kickname = kickname;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
