package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestSecurity implements Serializable {

    /**
     * 客户端生成的RSA公钥，用于数字签名校验
     */
    @NotBlank
    private String clientRSAPublicKey;

    /**
     * 客户端生成的数字签名
     */
    @NotBlank
    private String clientRSASignature;

    /**
     * 客户端生成的AES密钥，经过服务端生成的公钥加密
     * 通过服务端私钥解密后，即可使用
     */
    @NotBlank
    private String clientAESKey;

    public String getClientRSAPublicKey() {
        return clientRSAPublicKey;
    }

    public void setClientRSAPublicKey(String clientRSAPublicKey) {
        this.clientRSAPublicKey = clientRSAPublicKey;
    }

    public String getClientRSASignature() {
        return clientRSASignature;
    }

    public void setClientRSASignature(String clientRSASignature) {
        this.clientRSASignature = clientRSASignature;
    }

    public String getClientAESKey() {
        return clientAESKey;
    }

    public void setClientAESKey(String clientAESKey) {
        this.clientAESKey = clientAESKey;
    }
}
