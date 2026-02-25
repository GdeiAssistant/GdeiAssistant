package cn.gdeiassistant.core.alipay.pojo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付宝商户订单实体（占位，若存在订单表可扩展字段与 @Result 映射）。
 */
public class AlipayOrderEntity implements Serializable {

    private Long id;
    private String outTradeNo;
    private String tradeNo;
    private String tradeStatus;
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(String tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
