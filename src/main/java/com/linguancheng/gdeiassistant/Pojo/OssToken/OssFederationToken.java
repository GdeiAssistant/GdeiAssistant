package com.linguancheng.gdeiassistant.Pojo.OssToken;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class OssFederationToken {

    //表示获取Token的状态，获取成功时，返回值是200
    private String status;

    //表示Android/iOS应用初始化OSSClient获取的AccessKeyId
    private String accessKeyId;

    //表示Android/iOS应用初始化OSSClient获取AccessKeySecret
    private String accessKeySecret;

    //表示Android/iOS应用初始化的Token
    private String securityToken;

    //表示该Token失效的时间。主要在Android SDK会自动判断是否失效，自动获取Token
    private String expiration;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
}
