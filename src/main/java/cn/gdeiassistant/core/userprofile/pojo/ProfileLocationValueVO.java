package cn.gdeiassistant.core.userProfile.pojo;

import java.io.Serializable;

public class ProfileLocationValueVO implements Serializable {

    private String regionCode;
    private String stateCode;
    private String cityCode;

    public ProfileLocationValueVO() {
    }

    public ProfileLocationValueVO(String regionCode, String stateCode, String cityCode) {
        this.regionCode = regionCode;
        this.stateCode = stateCode;
        this.cityCode = cityCode;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }
}
