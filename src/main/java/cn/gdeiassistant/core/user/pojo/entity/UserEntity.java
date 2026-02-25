package cn.gdeiassistant.core.user.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;

/**
 * 用户表持久化实体，仅用于 MyBatis 映射与登录内部流转。
 * 表名、列名不变。
 */
public class UserEntity implements Serializable, Entity {

    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
