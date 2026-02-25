package cn.gdeiassistant.core.photograph.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 照片表 photograph 持久化实体。@Result column 与库表列名一致，property 驼峰。
 */
public class PhotographEntity implements Serializable, Entity {

    private Integer id;
    private String title;
    private String content;
    private Integer count;
    private Integer type;
    private String username;
    private Date createTime;
    private Integer likeCount;
    private Integer commentCount;
    private Integer liked;
    private List<PhotographCommentEntity> photographCommentList;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getLiked() { return liked; }
    public void setLiked(Integer liked) { this.liked = liked; }
    public List<PhotographCommentEntity> getPhotographCommentList() { return photographCommentList; }
    public void setPhotographCommentList(List<PhotographCommentEntity> photographCommentList) { this.photographCommentList = photographCommentList; }
}
