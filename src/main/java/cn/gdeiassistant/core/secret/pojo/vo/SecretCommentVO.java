package cn.gdeiassistant.core.secret.pojo.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 树洞评论视图。
 */
public class SecretCommentVO implements Serializable {

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
