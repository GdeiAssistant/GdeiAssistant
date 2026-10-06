package cn.gdeiassistant.core.express.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 表白评论公开视图：保留公开昵称，不返回校园 username。
 */
public class ExpressCommentVO implements Serializable {

    private Integer id;
    private Integer expressId;
    private String nickname;
    private String comment;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getExpressId() { return expressId; }
    public void setExpressId(Integer expressId) { this.expressId = expressId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
}
