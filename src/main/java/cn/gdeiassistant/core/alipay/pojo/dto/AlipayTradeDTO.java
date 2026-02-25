package cn.gdeiassistant.core.alipay.pojo.dto;

import java.io.Serializable;

/**
 * 支付宝统一下单/交易请求参数 DTO（占位，供后续支付流程使用）。
 */
public class AlipayTradeDTO implements Serializable {

    private String outTradeNo;
    private String subject;
    private String totalAmount;
    private String body;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
