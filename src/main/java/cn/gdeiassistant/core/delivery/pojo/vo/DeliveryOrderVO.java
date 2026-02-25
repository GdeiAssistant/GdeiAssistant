package cn.gdeiassistant.core.delivery.pojo.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 快递订单视图。
 */
public class DeliveryOrderVO implements Serializable {

    private Integer orderId;
    private String username;
    private Date orderTime;
    private String name;
    private String number;
    private String phone;
    private Float price;
    private String company;
    private String address;
    private Integer state;
    private String remarks;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Date getOrderTime() { return orderTime; }
    public void setOrderTime(Date orderTime) { this.orderTime = orderTime; }
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
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
