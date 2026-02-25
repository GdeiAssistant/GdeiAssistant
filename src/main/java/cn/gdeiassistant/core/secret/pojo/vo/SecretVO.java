package cn.gdeiassistant.core.secret.pojo.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 树洞列表/详情视图。
 */
public class SecretVO implements Serializable {

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
    private Integer liked;
    private String voiceURL;
    private List<SecretCommentVO> secretCommentList;

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
    public Integer getLiked() { return liked; }
    public void setLiked(Integer liked) { this.liked = liked; }
    public String getVoiceURL() { return voiceURL; }
    public void setVoiceURL(String voiceURL) { this.voiceURL = voiceURL; }
    public List<SecretCommentVO> getSecretCommentList() { return secretCommentList; }
    public void setSecretCommentList(List<SecretCommentVO> secretCommentList) { this.secretCommentList = secretCommentList; }
}
