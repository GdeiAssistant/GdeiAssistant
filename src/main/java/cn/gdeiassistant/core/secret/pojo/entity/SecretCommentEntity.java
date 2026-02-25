package cn.gdeiassistant.core.secret.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 树洞评论表 secret_comment 持久化实体。列名不变。
 */
public class SecretCommentEntity implements Serializable, Entity {

    private Integer id;
    private Integer contentId;
    private String username;
    private String comment;
    private Date publishTime;
    private Integer avatarTheme;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getContentId() { return contentId; }
    public void setContentId(Integer contentId) { this.contentId = contentId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public Integer getAvatarTheme() { return avatarTheme; }
    public void setAvatarTheme(Integer avatarTheme) { this.avatarTheme = avatarTheme; }
}
