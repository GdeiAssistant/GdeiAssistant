package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(value = {"handler", "secretCommentList"}, ignoreUnknown = true)
public class Secret implements Serializable {

    private String voiceURL;

    private Integer id;

    @Min(1)
    @Max(12)
    private Integer theme;

    @Size(max = 100)
    private String content;

    @Min(0)
    @Max(1)
    private Integer type;

    private List<SecretComment> secretCommentList;

    private Integer commentCount;

    private Integer likeCount;

    private Integer liked;

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
}
