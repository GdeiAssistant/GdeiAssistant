package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Privacy implements Serializable {

    private String username;

    private Boolean genderOpen;

    private Boolean genderOrientationOpen;

    private Boolean regionOpen;

    private Boolean introductionOpen;

    private Boolean facultyOpen;

    private Boolean majorOpen;

    private Boolean cacheAllow;

    public String isUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean isGenderOpen() {
        return genderOpen;
    }

    public void setGenderOpen(Boolean genderOpen) {
        this.genderOpen = genderOpen;
    }

    public Boolean isGenderOrientationOpen() {
        return genderOrientationOpen;
    }

    public void setGenderOrientationOpen(Boolean genderOrientationOpen) {
        this.genderOrientationOpen = genderOrientationOpen;
    }

    public Boolean isRegionOpen() {
        return regionOpen;
    }

    public void setRegionOpen(Boolean regionOpen) {
        this.regionOpen = regionOpen;
    }

    public Boolean isIntroductionOpen() {
        return introductionOpen;
    }

    public void setIntroductionOpen(Boolean introductionOpen) {
        this.introductionOpen = introductionOpen;
    }

    public Boolean isFacultyOpen() {
        return facultyOpen;
    }

    public void setFacultyOpen(Boolean facultyOpen) {
        this.facultyOpen = facultyOpen;
    }

    public Boolean isMajorOpen() {
        return majorOpen;
    }

    public void setMajorOpen(Boolean majorOpen) {
        this.majorOpen = majorOpen;
    }

    public Boolean isCacheAllow() {
        return cacheAllow;
    }

    public void setCacheAllow(Boolean cacheAllow) {
        this.cacheAllow = cacheAllow;
    }
}
