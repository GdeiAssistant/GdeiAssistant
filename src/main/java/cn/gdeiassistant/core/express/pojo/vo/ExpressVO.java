package cn.gdeiassistant.core.express.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 表白列表/详情视图，返回给前端。
 */
public class ExpressVO implements Serializable {

    private Integer id;
    private String username;
    private String nickname;
    private String realname;
    private Integer selfGender;
    private String name;
    private String content;
    private Integer personGender;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;
    private Integer likeCount;
    private Boolean liked;
    private Integer commentCount;
    private Integer guessCount;
    private Integer guessSum;
    private Boolean canGuess;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRealname() { return realname; }
    public void setRealname(String realname) { this.realname = realname; }
    public Integer getSelfGender() { return selfGender; }
    public void setSelfGender(Integer selfGender) { this.selfGender = selfGender; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getPersonGender() { return personGender; }
    public void setPersonGender(Integer personGender) { this.personGender = personGender; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getGuessCount() { return guessCount; }
    public void setGuessCount(Integer guessCount) { this.guessCount = guessCount; }
    public Integer getGuessSum() { return guessSum; }
    public void setGuessSum(Integer guessSum) { this.guessSum = guessSum; }
    public Boolean getCanGuess() { return canGuess; }
    public void setCanGuess(Boolean canGuess) { this.canGuess = canGuess; }
}
