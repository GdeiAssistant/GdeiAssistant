package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;
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

    private int id;

    @Min(1)
    @Max(12)
    private int theme;

    @NotBlank
    @Size(min = 1, max = 100)
    private String content;

    private List<SecretComment> secretCommentList;

    private int commentCount;

    private int likeCount;

    private int liked;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
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

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }
}
