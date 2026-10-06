package cn.gdeiassistant.core.delivery.pojo.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 快递交易视图。
 */
public class DeliveryTradeVO implements Serializable {

    private Integer tradeId;
    private Integer orderId;
    private Date createTime;
    private String authorId;
    private String displayName;
    private Integer state;

    public Integer getTradeId() { return tradeId; }
    public void setTradeId(Integer tradeId) { this.tradeId = tradeId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
}
