package cn.gdeiassistant.core.roommate.pojo.vo;

import java.io.Serializable;

/**
 * 撩一下记录视图。
 */
public class RoommatePickVO implements Serializable {

    private Integer pickId;
    private RoommateProfileVO roommateProfile;
    private String username;
    private String content;
    private Integer state;

    public Integer getPickId() { return pickId; }
    public void setPickId(Integer pickId) { this.pickId = pickId; }
    public RoommateProfileVO getRoommateProfile() { return roommateProfile; }
    public void setRoommateProfile(RoommateProfileVO roommateProfile) { this.roommateProfile = roommateProfile; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
}
