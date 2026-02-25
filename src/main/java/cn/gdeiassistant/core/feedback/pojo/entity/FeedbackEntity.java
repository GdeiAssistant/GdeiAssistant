package cn.gdeiassistant.core.feedback.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户反馈表 feedback 持久化实体。列名：id, username, content, contact, type, create_time。
 */
public class FeedbackEntity implements Serializable, Entity {

    private Integer id;
    private String username;
    private String content;
    private String contact;
    private String type;
    private Date createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
