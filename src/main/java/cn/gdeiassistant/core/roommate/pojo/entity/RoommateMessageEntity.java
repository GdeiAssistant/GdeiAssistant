package cn.gdeiassistant.core.roommate.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 卖室友消息表 dating_message 持久化实体。含嵌套 roommatePick。
 */
public class RoommateMessageEntity implements Serializable, Entity {

    private Integer messageId;
    private String username;
    private Integer type;
    private Integer state;
    private Date createTime;
    private RoommatePickEntity roommatePick;

    public Integer getMessageId() { return messageId; }
    public void setMessageId(Integer messageId) { this.messageId = messageId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public RoommatePickEntity getRoommatePick() { return roommatePick; }
    public void setRoommatePick(RoommatePickEntity roommatePick) { this.roommatePick = roommatePick; }
}
