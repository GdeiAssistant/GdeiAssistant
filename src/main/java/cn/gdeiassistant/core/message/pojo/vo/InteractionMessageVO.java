package cn.gdeiassistant.core.message.pojo.vo;

import java.io.Serializable;

public class InteractionMessageVO implements Serializable {

    private String id;
    private String module;
    private String type;
    private String title;
    private String content;
    private String createdAt;
    private Boolean isRead;
    private String targetType;
    private String targetId;
    private String targetSubId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetSubId() { return targetSubId; }
    public void setTargetSubId(String targetSubId) { this.targetSubId = targetSubId; }
}
