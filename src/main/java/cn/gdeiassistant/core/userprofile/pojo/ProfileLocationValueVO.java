package cn.gdeiassistant.core.userProfile.pojo;

import java.io.Serializable;

public class ProfileLocationValueVO implements Serializable {

    private String region;
    private String state;
    private String city;
    private String displayName;

    public ProfileLocationValueVO() {
    }

    public ProfileLocationValueVO(String region, String state, String city, String displayName) {
        this.region = region;
        this.state = state;
        this.city = city;
        this.displayName = displayName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
