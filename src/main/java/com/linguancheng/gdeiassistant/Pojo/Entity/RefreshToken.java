package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 刷新令牌用于刷新令牌的有效期，客户端调用刷新令牌接口时，服务端对刷新令牌进行校验，若校验通过则更新原令牌的有效期
 * 刷新令牌有效期为30天
 */

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshToken extends SimpleToken {

    /**
     * 该刷新令牌对应的令牌签名
     */
    private String tokenSignature;

    public String getTokenSignature() {
        return tokenSignature;
    }

    public void setTokenSignature(String tokenSignature) {
        this.tokenSignature = tokenSignature;
    }
}
