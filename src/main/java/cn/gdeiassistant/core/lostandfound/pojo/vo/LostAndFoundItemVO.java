package cn.gdeiassistant.core.lostandfound.pojo.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 失物招领列表/条目视图，返回给前端。
 */
public class LostAndFoundItemVO implements Serializable {

    private Integer id;
    private String username;
    private String name;
    private String description;
    private String location;
    private Integer itemType;
    private Integer lostType;
    private String qq;
    private String wechat;
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
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getItemType() { return itemType; }
    public void setItemType(Integer itemType) { this.itemType = itemType; }
    public Integer getLostType() { return lostType; }
    public void setLostType(Integer lostType) { this.lostType = lostType; }
    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }
    public String getWechat() { return wechat; }
    public void setWechat(String wechat) { this.wechat = wechat; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public List<String> getPictureURL() { return pictureURL; }
    public void setPictureURL(List<String> pictureURL) { this.pictureURL = pictureURL; }
}
