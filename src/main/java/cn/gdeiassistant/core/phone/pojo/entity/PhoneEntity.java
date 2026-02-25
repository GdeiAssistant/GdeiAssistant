package cn.gdeiassistant.core.phone.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * phone 表持久化实体。列名：username, code, phone, gmt_create, gmt_modified。
 */
public class PhoneEntity implements Serializable, Entity {

    private String username;
    private Integer code;
    private String phone;
    private Date createTime;
    private Date updateTime;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
