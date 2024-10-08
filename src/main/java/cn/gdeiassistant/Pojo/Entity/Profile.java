package cn.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile implements Serializable, Entity {

    private String avatarURL;

    private String username;

    private String nickname;

    private Date birthday;

    private Integer faculty;

    private Integer enrollment;

    private String major;

    private String locationRegion;

    private String locationState;

    private String locationCity;

    private String hometownRegion;

    private String hometownState;

    private String hometownCity;

    public String getLocationRegion() {
        return locationRegion;
    }

    public void setLocationRegion(String locationRegion) {
        this.locationRegion = locationRegion;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocationState() {
        return locationState;
    }

    public void setLocationState(String locationState) {
        this.locationState = locationState;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getHometownRegion() {
        return hometownRegion;
    }

    public void setHometownRegion(String hometownRegion) {
        this.hometownRegion = hometownRegion;
    }

    public String getHometownState() {
        return hometownState;
    }

    public void setHometownState(String hometownState) {
        this.hometownState = hometownState;
    }

    public String getHometownCity() {
        return hometownCity;
    }

    public void setHometownCity(String hometownCity) {
        this.hometownCity = hometownCity;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Integer getFaculty() {
        return faculty;
    }

    public void setFaculty(Integer faculty) {
        this.faculty = faculty;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Integer getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Integer enrollment) {
        this.enrollment = enrollment;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
