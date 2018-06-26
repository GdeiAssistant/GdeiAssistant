package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Privacy implements Serializable {

    private boolean gender;

    private boolean genderOrientation;

    private boolean region;

    private boolean introduction;

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public boolean isGenderOrientation() {
        return genderOrientation;
    }

    public void setGenderOrientation(boolean genderOrientation) {
        this.genderOrientation = genderOrientation;
    }

    public boolean isRegion() {
        return region;
    }

    public void setRegion(boolean region) {
        this.region = region;
    }

    public boolean isIntroduction() {
        return introduction;
    }

    public void setIntroduction(boolean introduction) {
        this.introduction = introduction;
    }
}
