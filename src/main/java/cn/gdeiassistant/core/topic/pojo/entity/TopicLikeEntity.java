package cn.gdeiassistant.core.topic.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 话题点赞表持久化实体。表 topic_like，列名不变。
 */
public class TopicLikeEntity implements Serializable, Entity {

    private Integer id;
    private Integer topicId;
    private String username;
    private Date createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
