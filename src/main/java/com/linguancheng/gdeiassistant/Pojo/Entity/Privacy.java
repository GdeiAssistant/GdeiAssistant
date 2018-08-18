package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Privacy implements Serializable {

    private Boolean gender;

    private Boolean genderOrientation;

    private Boolean region;

    private Boolean introduction;

    private Boolean cache;

    public Boolean isGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public Boolean isGenderOrientation() {
        return genderOrientation;
    }

    public void setGenderOrientation(Boolean genderOrientation) {
        this.genderOrientation = genderOrientation;
    }

    public Boolean isRegion() {
        return region;
    }

    public void setRegion(Boolean region) {
        this.region = region;
    }

    public Boolean isIntroduction() {
        return introduction;
    }

    public void setIntroduction(Boolean introduction) {
        this.introduction = introduction;
    }

    public Boolean isCache() {
        return cache;
    }

    public void setCache(Boolean cache) {
        this.cache = cache;
    }
}
