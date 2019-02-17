package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token implements Serializable {

    /**
     * 令牌签名
     */
    private String signature;

    /**
     * 创建时间戳
     */
    private Long createTime;

    /**
     * 过期时间戳
     */
    private Long expireTime;

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Token() {

    }

    public Token(AccessToken accessToken) {
        this.signature = accessToken.getSignature();
        this.createTime = accessToken.getCreateTime();
        this.expireTime = accessToken.getExpireTime();
    }

    public Token(RefreshToken refreshToken) {
        this.signature = refreshToken.getSignature();
        this.createTime = refreshToken.getCreateTime();
        this.expireTime = refreshToken.getExpireTime();
    }
}
