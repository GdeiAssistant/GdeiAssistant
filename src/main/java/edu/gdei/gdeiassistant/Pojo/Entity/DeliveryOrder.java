package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryOrder implements Serializable {

    //下单号
    private Integer orderId;

    //下单者用户名
    private String username;

    //下单时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:dd")
    private Date orderTime;

    //姓名
    @NotBlank
    @Length(min = 1, max = 10)
    private String name;

    //学号
    @NotBlank
    @Length(min = 11, max = 11)
    private String number;

    //手机号
    @NotBlank
    @Length(max = 11)
    private String phone;

    //报酬
    @NotNull
    private Float price;

    //快递公司
    @NotBlank
    @Length(min = 1, max = 10)
    private String company;

    //送往地址
    @NotBlank
    @Length(min = 1, max = 50)
    private String address;

    //状态，0为未被接单，1为已被接单，2为标记删除
    private Integer state;

    //备注信息
    @Length(max = 100)
    private String remarks;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
}
