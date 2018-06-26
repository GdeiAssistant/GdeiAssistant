package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErshouItem implements Serializable {

    private List<String> pictureURL;

    private int id;

    private String username;

    @NotBlank(message = "商品名词不能为空")
    @Length(max = 25)
    private String name;

    @NotBlank(message = "商品描述不能为空")
    @Length(max = 100)
    private String description;

    private float price;

    @NotBlank(message = "交易地点不能为空")
    @Length(max = 30)
    private String location;

    @Min(0)
    @Max(11)
    private int type;

    @NotBlank(message = "QQ不能为空")
    @Length(max = 20)
    private String qq;

    @Length(max = 11)
    private String phone;

    private int state;

    private Date publishTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
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

    public String getPublishTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(publishTime);
    }

    public void setPublishTime(String publishTime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.publishTime = simpleDateFormat.parse(publishTime);
    }
}
