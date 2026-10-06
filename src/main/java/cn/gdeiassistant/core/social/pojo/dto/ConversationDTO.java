package cn.gdeiassistant.core.social.pojo.dto;

import java.io.Serializable;

public class ConversationDTO implements Serializable {
    private String id;
    private SocialUserDTO peer;
    private ChatMessageDTO lastMessage;
    private String updatedAt;
    private int unreadCount;
    private String lastReadSeq;
    private boolean canSend;
    private String sendPermissionReason;
    private boolean imageMessagingEnabled;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public SocialUserDTO getPeer() { return peer; }
    public void setPeer(SocialUserDTO peer) { this.peer = peer; }
    public ChatMessageDTO getLastMessage() { return lastMessage; }
    public void setLastMessage(ChatMessageDTO lastMessage) { this.lastMessage = lastMessage; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public String getLastReadSeq() { return lastReadSeq; }
    public void setLastReadSeq(String lastReadSeq) { this.lastReadSeq = lastReadSeq; }
    public boolean isCanSend() { return canSend; }
    public void setCanSend(boolean canSend) { this.canSend = canSend; }
    public String getSendPermissionReason() { return sendPermissionReason; }
    public void setSendPermissionReason(String sendPermissionReason) { this.sendPermissionReason = sendPermissionReason; }
    public boolean isImageMessagingEnabled() { return imageMessagingEnabled; }
    public void setImageMessagingEnabled(boolean imageMessagingEnabled) {
        this.imageMessagingEnabled = imageMessagingEnabled;
    }
}
