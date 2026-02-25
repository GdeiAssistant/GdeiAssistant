package cn.gdeiassistant.core.roommate.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;

/**
 * 撩一下记录表 dating_pick 持久化实体。含嵌套 roommateProfile。
 */
public class RoommatePickEntity implements Serializable, Entity {

    private Integer pickId;
    private RoommateProfileEntity roommateProfile;
    private String username;
    private String content;
    private Integer state;

    public Integer getPickId() { return pickId; }
    public void setPickId(Integer pickId) { this.pickId = pickId; }
    public RoommateProfileEntity getRoommateProfile() { return roommateProfile; }
    public void setRoommateProfile(RoommateProfileEntity roommateProfile) { this.roommateProfile = roommateProfile; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
}
