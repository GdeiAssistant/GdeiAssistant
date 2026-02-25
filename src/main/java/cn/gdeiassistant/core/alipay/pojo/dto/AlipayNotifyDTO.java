package cn.gdeiassistant.core.alipay.pojo.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * 支付宝异步回调参数 DTO（占位，供后续回调验签与业务处理使用）。
 */
public class AlipayNotifyDTO implements Serializable {

    private Map<String, String> params;

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
