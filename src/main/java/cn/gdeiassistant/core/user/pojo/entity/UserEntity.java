package cn.gdeiassistant.core.user.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用账号 + 校园凭证联合视图（查询结果），仅用于 MyBatis 映射与登录内部流转。
 * 权威模型：app_user + campus_credential；campus_username 仍作为校园侧关联键。
 */
public class UserEntity implements Serializable, Entity {

    private Long id;
    private String publicId;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    /** 校园账号用户名（campus_credential.campus_username） */
    private String username;
    private String password;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
