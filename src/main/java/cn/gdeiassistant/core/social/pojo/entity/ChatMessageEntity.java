package cn.gdeiassistant.core.social.pojo.entity;

import java.util.Date;

public class ChatMessageEntity {
    private Long id;
    private Long conversationId;
    private Long seq;
    private Long senderId;
    private String clientMessageId;
    private String type;
    private String content;
    private String imageKey;
    private String imageContentType;
    private Integer imageWidth;
    private Integer imageHeight;
    private Integer imageSize;
    private String imageSha256;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSeq() { return seq; }
    public void setSeq(Long seq) { this.seq = seq; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getClientMessageId() { return clientMessageId; }
    public void setClientMessageId(String clientMessageId) { this.clientMessageId = clientMessageId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageKey() { return imageKey; }
    public void setImageKey(String imageKey) { this.imageKey = imageKey; }
    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }
    public Integer getImageWidth() { return imageWidth; }
    public void setImageWidth(Integer imageWidth) { this.imageWidth = imageWidth; }
    public Integer getImageHeight() { return imageHeight; }
    public void setImageHeight(Integer imageHeight) { this.imageHeight = imageHeight; }
    public Integer getImageSize() { return imageSize; }
    public void setImageSize(Integer imageSize) { this.imageSize = imageSize; }
    public String getImageSha256() { return imageSha256; }
    public void setImageSha256(String imageSha256) { this.imageSha256 = imageSha256; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
