package cn.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Authentication implements Serializable, Entity {

    /**
     * 实名认证记录ID
     */
    private Integer id;

    /**
     * 用户身份唯一标识，由SHA512(用户证件号+盐值)得到
     */
    private String identityCode;

    /**
     * 加密的用户名
     */
    private String username;

    /**
     * SHA512哈希的盐值
     */
    private String salt;

    /**
     * 实名认证信息创建时间
     */
    private Date createTime;

    /**
     * 实名认证信息更新时间
     */
    private Date updateTime;

    /**
     * 实名认证方式
     */
    private Integer type;

    public String getIdentityCode() {
        return identityCode;
    }

    public void setIdentityCode(String identityCode) {
        this.identityCode = identityCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
