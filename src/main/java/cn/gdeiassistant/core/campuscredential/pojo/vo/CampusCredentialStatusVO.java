package cn.gdeiassistant.core.campuscredential.pojo.vo;

import java.io.Serializable;
import java.util.Date;

public class CampusCredentialStatusVO implements Serializable {

    private Boolean hasActiveConsent;
    private Boolean hasSavedCredential;
    private Boolean quickAuthEnabled;
    private Date consentedAt;
    private Date revokedAt;
    private Date policyDate;
    private Date effectiveDate;
    private String maskedCampusAccount;

    public Boolean getHasActiveConsent() { return hasActiveConsent; }
    public void setHasActiveConsent(Boolean hasActiveConsent) { this.hasActiveConsent = hasActiveConsent; }
    public Boolean getHasSavedCredential() { return hasSavedCredential; }
    public void setHasSavedCredential(Boolean hasSavedCredential) { this.hasSavedCredential = hasSavedCredential; }
    public Boolean getQuickAuthEnabled() { return quickAuthEnabled; }
    public void setQuickAuthEnabled(Boolean quickAuthEnabled) { this.quickAuthEnabled = quickAuthEnabled; }
    public Date getConsentedAt() { return consentedAt; }
    public void setConsentedAt(Date consentedAt) { this.consentedAt = consentedAt; }
    public Date getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Date revokedAt) { this.revokedAt = revokedAt; }
    public Date getPolicyDate() { return policyDate; }
    public void setPolicyDate(Date policyDate) { this.policyDate = policyDate; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getMaskedCampusAccount() { return maskedCampusAccount; }
    public void setMaskedCampusAccount(String maskedCampusAccount) { this.maskedCampusAccount = maskedCampusAccount; }
}
