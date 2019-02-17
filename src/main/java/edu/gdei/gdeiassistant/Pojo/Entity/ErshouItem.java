package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErshouItem implements Serializable {

    /**
     * 商品图片
     */
    private List<String> pictureURL;

    /**
     * 商品ID
     */
    private Integer id;

    /**
     * 发布者用户名
     */
    private String username;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Length(max = 25)
    private String name;

    /**
     * 商品描述
     */
    @NotBlank(message = "商品描述不能为空")
    @Length(max = 100)
    private String description;

    /**
     * 商品价格
     */
    private Float price;

    /**
     * 交易地点
     */
    @NotBlank(message = "交易地点不能为空")
    @Length(max = 30)
    private String location;

    /**
     * 商品类型
     */
    @Min(0)
    @Max(11)
    private Integer type;

    /**
     * QQ号
     */
    @NotBlank(message = "QQ不能为空")
    @Length(max = 20)
    private String qq;

    /**
     * 手机号
     */
    @Length(max = 11)
    private String phone;

    /**
     * 商品状态
     * 0为下架
     * 1为待出售
     * 2为已出售
     */
    private Integer state;

    /**
     * 发布时间
     */
    private Date publishTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public List<String> getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(List<String> pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }
}
