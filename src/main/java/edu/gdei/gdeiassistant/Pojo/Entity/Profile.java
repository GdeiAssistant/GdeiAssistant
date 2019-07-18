package edu.gdei.gdeiassistant.Pojo.Entity;

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

    private String kickname;

    private Integer gender;

    private Date birthday;

    private Integer degree;

    private Integer faculty;

    private Integer enrollment;

    private String major;

    private String customGenderName;

    private String region;

    private String state;

    private String city;

    private String primarySchool;

    private String juniorHighSchool;

    private String highSchool;

    public String getPrimarySchool() {
        return primarySchool;
    }

    public void setPrimarySchool(String primarySchool) {
        this.primarySchool = primarySchool;
    }

    public String getJuniorHighSchool() {
        return juniorHighSchool;
    }

    public void setJuniorHighSchool(String juniorHighSchool) {
        this.juniorHighSchool = juniorHighSchool;
    }

    public String getHighSchool() {
        return highSchool;
    }

    public void setHighSchool(String highSchool) {
        this.highSchool = highSchool;
    }

    public String getKickname() {
        return kickname;
    }

    public void setKickname(String kickname) {
        this.kickname = kickname;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getCustomGenderName() {
        return customGenderName;
    }

    public void setCustomGenderName(String customGenderName) {
        this.customGenderName = customGenderName;
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

    public Integer getDegree() {
        return degree;
    }

    public void setDegree(Integer degree) {
        this.degree = degree;
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
}
