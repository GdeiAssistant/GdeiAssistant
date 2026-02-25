package cn.gdeiassistant.core.delivery.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

/**
 * delivery_trade 表持久化实体。
 */
public class DeliveryTradeEntity implements Serializable, Entity {

    private Integer tradeId;
    private Integer orderId;
    private Date createTime;
    private String username;
    private Integer state;

    public Integer getTradeId() { return tradeId; }
    public void setTradeId(Integer tradeId) { this.tradeId = tradeId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
}
