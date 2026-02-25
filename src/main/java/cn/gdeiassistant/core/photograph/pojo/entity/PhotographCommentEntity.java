package cn.gdeiassistant.core.photograph.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 照片评论表 photograph_comment 持久化实体。列名 comment_id, photo_id, username, nickname, comment, create_time。
 */
public class PhotographCommentEntity implements Serializable, Entity {

    private Integer commentId;
    private Integer photoId;
    private String username;
    private String nickname;
    private String comment;
    private Date createTime;

    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }
    public Integer getPhotoId() { return photoId; }
    public void setPhotoId(Integer photoId) { this.photoId = photoId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
