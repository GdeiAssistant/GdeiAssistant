package cn.gdeiassistant.core.cetquery.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;

/**
 * cet 表持久化实体（username, number）。
 */
public class CetNumberEntity implements Serializable, Entity {

    private String username;
    private Long number;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getNumber() { return number; }
    public void setNumber(Long number) { this.number = number; }
}
