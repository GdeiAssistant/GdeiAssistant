package cn.gdeiassistant.core.marketplace.pojo.dto;

import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 前端发布二手商品时的入参 DTO。
 */
public class MarketplacePublishDTO implements Serializable {

    @NotBlank(message = "商品名称不能为空")
    @Length(max = 25)
    private String name;

    @NotBlank(message = "商品描述不能为空")
    @Length(max = 100)
    private String description;

    @NotNull
    private Float price;

    @NotBlank(message = "交易地点不能为空")
    @Length(max = 30)
    private String location;

    @NotNull
    @Min(0)
    @Max(11)
    private Integer type;

    @Length(max = 20)
    private String qq;

    @Length(max = 11)
    private String phone;

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
}
