package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile implements Serializable {

    private String username;

    private String kickname;

    private String realname;

    private Integer gender;

    private Integer genderOrientation;

    private Integer faculty;

    private String major;

    private String customGenderName;

    private String region;

    private String state;

    private String city;

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

    public Integer getGenderOrientation() {
        return genderOrientation;
    }

    public void setGenderOrientation(int genderOrientation) {
        this.genderOrientation = genderOrientation;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
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
}
