package cn.gdeiassistant.core.lostandfound.pojo.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;

/**
 * 前端发布/更新失物招领时的入参 DTO。
 */
public class LostAndFoundPublishDTO implements Serializable {

    @NotBlank(message = "物品名称不能为空")
    @Length(max = 25)
    private String name;

    @NotBlank(message = "物品描述不能为空")
    @Length(max = 100)
    private String description;

    @NotBlank(message = "地点不能为空")
    @Length(max = 30)
    private String location;

    @Min(0)
    @Max(11)
    private Integer itemType;

    @Min(0)
    @Max(1)
    private Integer lostType;

    @Length(max = 20)
    private String qq;

    @Length(max = 20)
    private String wechat;

    @Length(max = 11)
    private String phone;

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
}
