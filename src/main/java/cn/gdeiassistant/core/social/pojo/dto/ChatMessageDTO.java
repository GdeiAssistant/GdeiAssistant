package cn.gdeiassistant.core.social.pojo.dto;

import java.io.Serializable;

public class ChatMessageDTO implements Serializable {
    private String id;
    private String conversationId;
    private String seq;
    private String senderId;
    private String clientMessageId;
    private String type;
    private String content;
    private String createdAt;
    private ChatImageDTO image;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getSeq() { return seq; }
    public void setSeq(String seq) { this.seq = seq; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(String clientMessageId) { this.clientMessageId = clientMessageId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public ChatImageDTO getImage() { return image; }
    public void setImage(ChatImageDTO image) { this.image = image; }
}
