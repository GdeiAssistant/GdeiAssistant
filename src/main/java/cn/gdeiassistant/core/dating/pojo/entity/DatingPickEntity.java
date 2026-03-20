package cn.gdeiassistant.core.dating.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;

/**
 * 撩一下记录表 dating_pick 持久化实体。含嵌套 datingProfile。
 */
public class DatingPickEntity implements Serializable, Entity {

    private Integer pickId;
    private DatingProfileEntity datingProfile;
    private String username;
    private String content;
    private Integer state;

    public Integer getPickId() { return pickId; }
    public void setPickId(Integer pickId) { this.pickId = pickId; }
    public DatingProfileEntity getRoommateProfile() { return datingProfile; }
    public void setRoommateProfile(DatingProfileEntity datingProfile) { this.datingProfile = datingProfile; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
}
