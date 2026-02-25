package cn.gdeiassistant.core.charge.pojo.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * 充值结果视图：支付宝 URL + Cookie 列表 + 签名（与前端约定）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChargeVO implements Serializable {

    @JSONField(ordinal = 1)
    private String alipayURL;

    @JSONField(ordinal = 2)
    private List<cn.gdeiassistant.common.pojo.Entity.Cookie> cookieList;

    private String signature;

    public String getAlipayURL() {
        return alipayURL;
    }

    public void setAlipayURL(String alipayURL) {
        this.alipayURL = alipayURL;
    }

    public List<cn.gdeiassistant.common.pojo.Entity.Cookie> getCookieList() {
        return cookieList;
    }

    public void setCookieList(List<cn.gdeiassistant.common.pojo.Entity.Cookie> cookieList) {
        this.cookieList = cookieList;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
