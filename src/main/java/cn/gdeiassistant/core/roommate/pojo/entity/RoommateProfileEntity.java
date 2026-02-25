package cn.gdeiassistant.core.roommate.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;

/**
 * 卖室友信息表 dating_profile 持久化实体。@Result column 与库表列名一致。
 */
public class RoommateProfileEntity implements Serializable, Entity {

    private Integer profileId;
    private String username;
    private String nickname;
    private Integer grade;
    private String faculty;
    private String hometown;
    private String content;
    private String qq;
    private String wechat;
    private Integer area;
    private Integer state;

    public Integer getProfileId() { return profileId; }
    public void setProfileId(Integer profileId) { this.profileId = profileId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
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
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
}
