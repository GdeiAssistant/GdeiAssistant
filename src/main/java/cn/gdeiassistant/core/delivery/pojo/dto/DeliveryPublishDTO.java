package cn.gdeiassistant.core.delivery.pojo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 发布快递代收订单入参 DTO。
 */
public class DeliveryPublishDTO implements Serializable {

    @NotBlank
    @Length(min = 1, max = 10)
    private String name;

    @NotBlank
    @Length(min = 11, max = 11)
    private String number;

    @NotBlank
    @Length(max = 11)
    private String phone;

    @NotNull
    private Float price;

    @NotBlank
    @Length(min = 1, max = 10)
    private String company;

    @NotBlank
    @Length(min = 1, max = 50)
    private String address;

    @Length(max = 100)
    private String remarks;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
