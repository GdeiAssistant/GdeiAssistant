package cn.gdeiassistant.core.userProfile.pojo;

import java.io.Serializable;

public class ProfileFacultyValueVO implements Serializable {

    private Integer code;
    private String label;

    public ProfileFacultyValueVO() {
    }

    public ProfileFacultyValueVO(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
