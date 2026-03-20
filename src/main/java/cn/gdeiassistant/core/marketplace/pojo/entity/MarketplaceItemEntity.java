package cn.gdeiassistant.core.marketplace.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 二手商品持久化实体，仅用于 MyBatis 映射与 Service 内部。
 * 数据库表名、列名不变，通过 @Result(column = "xxx") 映射。
 */
public class MarketplaceItemEntity implements Serializable, Entity {

    private Integer id;
    private String username;
    private String name;
    private String description;
    private Float price;
    private String location;
    private Integer type;
    private String qq;
    private String phone;
    private Integer state;
    private Date publishTime;
    private List<String> pictureURL;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public List<String> getPictureURL() { return pictureURL; }
    public void setPictureURL(List<String> pictureURL) { this.pictureURL = pictureURL; }
}
