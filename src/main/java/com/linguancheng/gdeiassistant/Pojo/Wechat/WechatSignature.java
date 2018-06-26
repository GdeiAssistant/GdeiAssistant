package com.linguancheng.gdeiassistant.Pojo.Wechat;

import org.hibernate.validator.constraints.NotBlank;

public class WechatSignature {

    @NotBlank(message = "微信加密签名不能为空")
    private String signature;

    @NotBlank(message = "时间戳不能为空")
    private String timestamp;

    @NotBlank(message = "随机数不能为空")
    private String nonce;

    @NotBlank(message = "随机字符串不能为空")
    private String echostr;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getEchostr() {
        return echostr;
    }

    public void setEchostr(String echostr) {
        this.echostr = echostr;
    }
}
