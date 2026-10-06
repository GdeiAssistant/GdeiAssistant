package cn.gdeiassistant.core.delivery.pojo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 发布快递代收订单入参 DTO。
 */
public class DeliveryPublishDTO implements Serializable {

    @NotBlank
    @Length(min = 1, max = 10)
    private String taskName;

    @Length(max = 64)
    private String pickupCode;

    @NotBlank
    @Length(max = 11)
    private String contactPhone;

    @NotNull
    @DecimalMin(value = "0.01", message = "价格必须大于零")
    @DecimalMax(value = "9999.99", message = "价格超出限制")
    @jakarta.validation.constraints.Digits(integer = 4, fraction = 2)
    private BigDecimal price;

    @NotBlank
    @Length(min = 1, max = 100)
    private String pickupLocation;

    @NotBlank
    @Length(min = 1, max = 50)
    private String deliveryAddress;

    @Length(max = 100)
    private String remarks;

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
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
