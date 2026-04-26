package cn.gdeiassistant.core.campuscredential.pojo.dto;

import java.io.Serializable;

public class CampusCredentialQuickAuthUpdateDTO implements Serializable {

    private boolean enabled;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
