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

    private Boolean regionOpen;

    private Boolean introductionOpen;

    private Boolean facultyOpen;

    private Boolean majorOpen;

    private Boolean enrollmentOpen;

    private Boolean ageOpen;

    private Boolean degreeOpen;

    private Boolean primarySchoolOpen;

    private Boolean juniorHighSchoolOpen;

    private Boolean highSchoolOpen;

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
        this.genderOpen = genderOpen;
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

    public Boolean isAgeOpen() {
        return ageOpen;
    }

    public void setAgeOpen(Boolean ageOpen) {
        this.ageOpen = ageOpen;
    }

    public Boolean isDegreeOpen() {
        return degreeOpen;
    }

    public void setDegreeOpen(Boolean degreeOpen) {
        this.degreeOpen = degreeOpen;
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

    public Boolean isEnrollmentOpen() {
        return enrollmentOpen;
    }

    public void setEnrollmentOpen(Boolean enrollmentOpen) {
        this.enrollmentOpen = enrollmentOpen;
    }

    public Boolean isPrimarySchoolOpen() {
        return primarySchoolOpen;
    }

    public void setPrimarySchoolOpen(Boolean primarySchoolOpen) {
        this.primarySchoolOpen = primarySchoolOpen;
    }

    public Boolean isJuniorHighSchoolOpen() {
        return juniorHighSchoolOpen;
    }

    public void setJuniorHighSchoolOpen(Boolean juniorHighSchoolOpen) {
        this.juniorHighSchoolOpen = juniorHighSchoolOpen;
    }

    public Boolean isHighSchoolOpen() {
        return highSchoolOpen;
    }

    public void setHighSchoolOpen(Boolean highSchoolOpen) {
        this.highSchoolOpen = highSchoolOpen;
    }

    public Boolean isProfessionOpen() {
        return professionOpen;
    }

    public void setProfessionOpen(Boolean professionOpen) {
        this.professionOpen = professionOpen;
    }
}
