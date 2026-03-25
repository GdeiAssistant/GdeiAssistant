package cn.gdeiassistant.core.userProfile.pojo;

import java.io.Serializable;

public class ProfileMajorValueVO implements Serializable {

    private String code;
    private String label;

    public ProfileMajorValueVO() {
    }

    public ProfileMajorValueVO(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
