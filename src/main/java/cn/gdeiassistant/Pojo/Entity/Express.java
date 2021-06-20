package cn.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Express implements Serializable, Entity {

    /**
     * 表白信息ID
     */
    private Integer id;

    /**
     * 发布者用户名
     */
    private String username;

    /**
     * 发布者昵称
     */
    @NotBlank
    @Length(min = 1, max = 10)
    private String nickname;

    /**
     * 发布者真名，用于参与猜猜看游戏
     */
    @Length(max = 10)
    private String realname;

    /**
     * 表白者的性别
     * <p>
     * 0为男，1为女，2为其他或保密
     */
    @NotNull
    @Min(0)
    @Max(2)
    private Integer selfGender;

    /**
     * 被表白者的名称
     */
    @NotBlank
    @Length(min = 1, max = 10)
    private String name;

    /**
     * 表白内容
     */
    @NotBlank
    @Length(min = 1, max = 250)
    private String content;

    /**
     * 被表白者的性别
     * <p>
     * 0为男，1为女，2为其他或保密
     */
    @NotNull
    @Min(0)
    @Max(2)
    private Integer personGender;

    /**
     * 表白信息发布时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否已点赞
     */
    private Boolean liked;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 猜一下猜中的次数
     */
    private Integer guessCount;

    /**
     * 猜一下总次数
     */
    private Integer guessSum;

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public Integer getSelfGender() {
        return selfGender;
    }

    public void setSelfGender(Integer selfGender) {
        this.selfGender = selfGender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getPersonGender() {
        return personGender;
    }

    public void setPersonGender(Integer personGender) {
        this.personGender = personGender;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getGuessCount() {
        return guessCount;
    }

    public void setGuessCount(Integer guessCount) {
        this.guessCount = guessCount;
    }

    public Integer getGuessSum() {
        return guessSum;
    }

    public void setGuessSum(Integer guessSum) {
        this.guessSum = guessSum;
    }
}
