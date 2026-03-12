package cn.gdeiassistant.core.secret.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 树洞点赞表 secret_like 持久化实体。
 */
public class SecretLikeEntity implements Serializable, Entity {

    private Integer id;
    private Integer contentId;
    private String username;
    private Date createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getContentId() { return contentId; }
    public void setContentId(Integer contentId) { this.contentId = contentId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
