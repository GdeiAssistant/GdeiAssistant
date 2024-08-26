package cn.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Privacy implements Serializable, Entity {

    private String username;

    private Boolean locationOpen;

    private Boolean hometownOpen;

    private Boolean introductionOpen;

    private Boolean facultyOpen;

    private Boolean majorOpen;

    private Boolean enrollmentOpen;

    private Boolean ageOpen;

    private Boolean cacheAllow;

    private Boolean robotsIndexAllow;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean isLocationOpen() {
        return locationOpen;
    }

    public void setLocationOpen(Boolean locationOpen) {
        this.locationOpen = locationOpen;
    }

    public Boolean isHometownOpen() {
        return hometownOpen;
    }

    public void setHometownOpen(Boolean hometownOpen) {
        this.hometownOpen = hometownOpen;
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

    public Boolean isEnrollmentOpen() {
        return enrollmentOpen;
    }

    public void setEnrollmentOpen(Boolean enrollmentOpen) {
        this.enrollmentOpen = enrollmentOpen;
    }

    public Boolean isAgeOpen() {
        return ageOpen;
    }

    public void setAgeOpen(Boolean ageOpen) {
        this.ageOpen = ageOpen;
    }

    public Boolean isCacheAllow() {
        return cacheAllow;
    }

    public void setCacheAllow(Boolean cacheAllow) {
        this.cacheAllow = cacheAllow;
    }

    public Boolean isRobotsIndexAllow() {
        return robotsIndexAllow;
    }

    public void setRobotsIndexAllow(Boolean robotsIndexAllow) {
        this.robotsIndexAllow = robotsIndexAllow;
    }
}
