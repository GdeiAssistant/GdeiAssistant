package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
@Scope("prototype")
public class LostAndFoundItem implements Serializable {

    /**
     * 物品图片URL链接
     */
    private List<String> pictureURL;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 发布者用户名
     */
    private String username;

    /**
     * 物品名称
     */
    @NotBlank
    @Length(max = 25)
    private String name;

    /**
     * 物品描述
     */
    @NotBlank
    @Length(max = 100)
    private String description;

    /**
     * 丢失或捡到的地点
     */
    @NotBlank
    @Length(max = 30)
    private String location;

    /**
     * 物品类别
     */
    @Min(0)
    @Max(11)
    private Integer itemType;

    /**
     * 丢失类别
     * 0为丢失——寻主
     * 1为招领——寻物
     */
    @Min(0)
    @Max(1)
    private Integer lostType;

    /**
     * 联系QQ
     */
    @Length(max = 20)
    private String qq;

    /**
     * 联系微信号
     */
    @Length(max = 20)
    private String wechat;

    /**
     * 联系手机号
     */
    @Length(max = 11)
    private String phone;

    /**
     * 物品状态
     * 0为寻主/寻物中
     * 1为确认寻回
     */
    private Integer state;

    /**
     * 发布时间
     */
    private Date publishTime;

    public List<String> getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(List<String> pictureURL) {
        this.pictureURL = pictureURL;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public Integer getLostType() {
        return lostType;
    }

    public void setLostType(Integer lostType) {
        this.lostType = lostType;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getPublishTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(publishTime);
    }

    public void setPublishTime(String publishTime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.publishTime = simpleDateFormat.parse(publishTime);
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    /**
     * 是否包含联系信息
     *
     * @return
     */
    public boolean containsContactInfo() {
        return StringUtils.isNotBlank(qq) || StringUtils.isNotBlank(wechat) || StringUtils.isNotBlank(phone);
    }
}
