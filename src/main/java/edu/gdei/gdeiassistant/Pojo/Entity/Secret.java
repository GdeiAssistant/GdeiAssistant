package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(value = {"handler", "secretCommentList"}, ignoreUnknown = true)
public class Secret implements Serializable {

    /**
     * 语音类型树洞消息语音文件列表
     */
    private String voiceURL;

    /**
     * 校园树洞ID
     */
    private Integer id;

    /**
     * 校园树洞主题ID
     */
    @Min(1)
    @Max(12)
    private Integer theme;

    /**
     * 文本类型树洞消息内容
     */
    @Size(max = 100)
    private String content;

    /**
     * 树洞消息类型，0为文本类型，1为语音类型
     */
    @Min(0)
    @Max(1)
    private Integer type;

    /**
     * 评论列表
     */
    private List<SecretComment> secretCommentList;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Integer liked;

    /**
     * 是否定时删除，0为不启用，1为启用
     */
    @Min(0)
    @Max(1)
    private Integer timer;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 树洞消息信息状态，0为已发布，1为被定时删除，2为管理员删除
     */
    @Min(0)
    @Max(2)
    private Integer state;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTheme() {
        return theme;
    }

    public void setTheme(Integer theme) {
        this.theme = theme;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<SecretComment> getSecretCommentList() {
        return secretCommentList;
    }

    public void setSecretCommentList(List<SecretComment> secretCommentList) {
        this.secretCommentList = secretCommentList;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getVoiceURL() {
        return voiceURL;
    }

    public void setVoiceURL(String voiceURL) {
        this.voiceURL = voiceURL;
    }

    public Integer getTimer() {
        return timer;
    }

    public void setTimer(Integer timer) {
        this.timer = timer;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }
}
