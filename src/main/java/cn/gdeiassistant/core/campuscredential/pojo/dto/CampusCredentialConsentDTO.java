package cn.gdeiassistant.core.campuscredential.pojo.dto;

import java.io.Serializable;

public class CampusCredentialConsentDTO implements Serializable {

    private String scene;
    private String policyDate;
    private String effectiveDate;

    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getPolicyDate() { return policyDate; }
    public void setPolicyDate(String policyDate) { this.policyDate = policyDate; }
    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }
}
