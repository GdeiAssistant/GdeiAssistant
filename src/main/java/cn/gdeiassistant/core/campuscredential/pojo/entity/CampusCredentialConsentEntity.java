package cn.gdeiassistant.core.campuscredential.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;
import java.util.Date;

public class CampusCredentialConsentEntity implements Serializable, Entity {

    private Long id;
    private String username;
    private String consentType;
    private Date policyDate;
    private Date effectiveDate;
    private String scene;
    private Date consentedAt;
    private Date revokedAt;
    private String revokedReason;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }
    public Date getPolicyDate() { return policyDate; }
    public void setPolicyDate(Date policyDate) { this.policyDate = policyDate; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public Date getConsentedAt() { return consentedAt; }
    public void setConsentedAt(Date consentedAt) { this.consentedAt = consentedAt; }
    public Date getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Date revokedAt) { this.revokedAt = revokedAt; }
    public String getRevokedReason() { return revokedReason; }
    public void setRevokedReason(String revokedReason) { this.revokedReason = revokedReason; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
