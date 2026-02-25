package cn.gdeiassistant.core.secret.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 树洞内容表 secret_content 持久化实体。列名不变。
 */
public class SecretContentEntity implements Serializable, Entity {

    private Integer id;
    private String username;
    private String content;
    private Integer theme;
    private Integer type;
    private Integer timer;
    private Integer state;
    private Date publishTime;
    private Integer likeCount;
    private Integer commentCount;
    private List<SecretCommentEntity> secretCommentList;
    private String voiceURL;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getTheme() { return theme; }
    public void setTheme(Integer theme) { this.theme = theme; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getTimer() { return timer; }
    public void setTimer(Integer timer) { this.timer = timer; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public List<SecretCommentEntity> getSecretCommentList() { return secretCommentList; }
    public void setSecretCommentList(List<SecretCommentEntity> secretCommentList) { this.secretCommentList = secretCommentList; }
    public String getVoiceURL() { return voiceURL; }
    public void setVoiceURL(String voiceURL) { this.voiceURL = voiceURL; }
}
