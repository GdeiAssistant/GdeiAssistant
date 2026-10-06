package cn.gdeiassistant.core.social.pojo.entity;

import java.util.Date;

public class ConversationEntity {
    private Long id;
    private Long userLowId;
    private Long userHighId;
    private Long lastSeq;
    private Date lastMessageAt;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserLowId() { return userLowId; }
    public void setUserLowId(Long userLowId) { this.userLowId = userLowId; }
    public Long getUserHighId() { return userHighId; }
    public void setUserHighId(Long userHighId) { this.userHighId = userHighId; }
    public Long getLastSeq() { return lastSeq; }
    public void setLastSeq(Long lastSeq) { this.lastSeq = lastSeq; }
    public Date getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Date lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
