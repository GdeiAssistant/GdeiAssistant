package cn.gdeiassistant.core.delivery.pojo.entity;

import cn.gdeiassistant.common.pojo.entity.Entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * delivery_order 表持久化实体。@Result column 与库表列名一致。
 */
public class DeliveryOrderEntity implements Serializable, Entity {

    private Integer orderId;
    private String username;
    private Date orderTime;
    private String taskName;
    private String pickupCode;
    private String contactPhone;
    private BigDecimal price;
    private String pickupLocation;
    private String deliveryAddress;
    private Integer state;
    private String remarks;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Date getOrderTime() { return orderTime; }
    public void setOrderTime(Date orderTime) { this.orderTime = orderTime; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
