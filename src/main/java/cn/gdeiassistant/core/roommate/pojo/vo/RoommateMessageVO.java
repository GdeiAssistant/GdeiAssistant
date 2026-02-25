package cn.gdeiassistant.core.roommate.pojo.vo;

import java.io.Serializable;

/**
 * 卖室友消息视图。
 */
public class RoommateMessageVO implements Serializable {

    private Integer messageId;
    private String username;
    private Integer type;
    private Integer state;
    private RoommatePickVO roommatePick;

    public Integer getMessageId() { return messageId; }
    public void setMessageId(Integer messageId) { this.messageId = messageId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public RoommatePickVO getRoommatePick() { return roommatePick; }
    public void setRoommatePick(RoommatePickVO roommatePick) { this.roommatePick = roommatePick; }
}
