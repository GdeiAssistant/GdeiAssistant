package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Privacy implements Serializable, Entity {

    private String username;

    private Boolean genderOpen;

    private Boolean locationOpen;

    private Boolean hometownOpen;

    private Boolean introductionOpen;

    private Boolean facultyOpen;

    private Boolean majorOpen;

    private Boolean enrollmentOpen;

    private Boolean ageOpen;

    private Boolean degreeOpen;

    private Boolean primarySchoolOpen;

    private Boolean juniorHighSchoolOpen;

    private Boolean highSchoolOpen;

    private Boolean collegesOpen;

    private Boolean professionOpen;

    private Boolean cacheAllow;

    private Boolean robotsIndexAllow;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean isGenderOpen() {
        return genderOpen;
    }

    public void setGenderOpen(Boolean genderOpen) {
        this.genderOpen = Boolean.FALSE.equals(genderOpen);
    }

    public Boolean isLocationOpen() {
        return locationOpen;
    }

    public void setLocationOpen(Boolean locationOpen) {
        this.locationOpen = Boolean.FALSE.equals(locationOpen);
    }

    public Boolean isHometownOpen() {
        return hometownOpen;
    }

    public void setHometownOpen(Boolean hometownOpen) {
        this.hometownOpen = Boolean.FALSE.equals(hometownOpen);
    }

    public Boolean isIntroductionOpen() {
        return introductionOpen;
    }

    public void setIntroductionOpen(Boolean introductionOpen) {
        this.introductionOpen = Boolean.FALSE.equals(introductionOpen);
    }

    public Boolean isFacultyOpen() {
        return facultyOpen;
    }

    public void setFacultyOpen(Boolean facultyOpen) {
        this.facultyOpen = Boolean.FALSE.equals(facultyOpen);
    }

    public Boolean isMajorOpen() {
        return majorOpen;
    }

    public void setMajorOpen(Boolean majorOpen) {
        this.majorOpen = Boolean.FALSE.equals(majorOpen);
    }

    public Boolean isCacheAllow() {
        return cacheAllow;
    }

    public Boolean isAgeOpen() {
        return ageOpen;
    }

    public void setAgeOpen(Boolean ageOpen) {
        this.ageOpen = Boolean.FALSE.equals(ageOpen);
    }

    public Boolean isDegreeOpen() {
        return degreeOpen;
    }

    public void setDegreeOpen(Boolean degreeOpen) {
        this.degreeOpen = Boolean.FALSE.equals(degreeOpen);
    }

    public void setCacheAllow(Boolean cacheAllow) {
        this.cacheAllow = Boolean.FALSE.equals(cacheAllow);
    }

    public Boolean isRobotsIndexAllow() {
        return robotsIndexAllow;
    }

    public void setRobotsIndexAllow(Boolean robotsIndexAllow) {
        this.robotsIndexAllow = Boolean.FALSE.equals(robotsIndexAllow);
    }

    public Boolean isEnrollmentOpen() {
        return enrollmentOpen;
    }

    public void setEnrollmentOpen(Boolean enrollmentOpen) {
        this.enrollmentOpen = Boolean.FALSE.equals(enrollmentOpen);
    }

    public Boolean isPrimarySchoolOpen() {
        return primarySchoolOpen;
    }

    public void setPrimarySchoolOpen(Boolean primarySchoolOpen) {
        this.primarySchoolOpen = Boolean.FALSE.equals(primarySchoolOpen);
    }

    public Boolean isJuniorHighSchoolOpen() {
        return juniorHighSchoolOpen;
    }

    public void setJuniorHighSchoolOpen(Boolean juniorHighSchoolOpen) {
        this.juniorHighSchoolOpen = Boolean.FALSE.equals(juniorHighSchoolOpen);
    }

    public Boolean isHighSchoolOpen() {
        return highSchoolOpen;
    }

    public void setHighSchoolOpen(Boolean highSchoolOpen) {
        this.highSchoolOpen = Boolean.FALSE.equals(highSchoolOpen);
    }

    public Boolean isProfessionOpen() {
        return professionOpen;
    }

    public void setProfessionOpen(Boolean professionOpen) {
        this.professionOpen = Boolean.FALSE.equals(professionOpen);
    }

    public Boolean isCollegesOpen() {
        return collegesOpen;
    }

    public void setCollegesOpen(Boolean collegesOpen) {
        this.collegesOpen = Boolean.FALSE.equals(collegesOpen);
    }
}
