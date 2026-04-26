package cn.gdeiassistant.core.privacy.pojo.vo;

import java.io.Serializable;

/**
 * 隐私设置视图。
 */
public class PrivacyVO implements Serializable {

    private String username;
    private Boolean locationOpen;
    private Boolean hometownOpen;
    private Boolean introductionOpen;
    private Boolean facultyOpen;
    private Boolean majorOpen;
    private Boolean enrollmentOpen;
    private Boolean ageOpen;
    private Boolean cacheAllow;
    private Boolean quickAuthAllow;
    private Boolean robotsIndexAllow;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Boolean getLocationOpen() { return locationOpen; }
    public void setLocationOpen(Boolean locationOpen) { this.locationOpen = locationOpen; }
    public Boolean getHometownOpen() { return hometownOpen; }
    public void setHometownOpen(Boolean hometownOpen) { this.hometownOpen = hometownOpen; }
    public Boolean getIntroductionOpen() { return introductionOpen; }
    public void setIntroductionOpen(Boolean introductionOpen) { this.introductionOpen = introductionOpen; }
    public Boolean getFacultyOpen() { return facultyOpen; }
    public void setFacultyOpen(Boolean facultyOpen) { this.facultyOpen = facultyOpen; }
    public Boolean getMajorOpen() { return majorOpen; }
    public void setMajorOpen(Boolean majorOpen) { this.majorOpen = majorOpen; }
    public Boolean getEnrollmentOpen() { return enrollmentOpen; }
    public void setEnrollmentOpen(Boolean enrollmentOpen) { this.enrollmentOpen = enrollmentOpen; }
    public Boolean getAgeOpen() { return ageOpen; }
    public void setAgeOpen(Boolean ageOpen) { this.ageOpen = ageOpen; }
    public Boolean getCacheAllow() { return cacheAllow; }
    public void setCacheAllow(Boolean cacheAllow) { this.cacheAllow = cacheAllow; }
    public Boolean getQuickAuthAllow() { return quickAuthAllow; }
    public void setQuickAuthAllow(Boolean quickAuthAllow) { this.quickAuthAllow = quickAuthAllow; }
    public Boolean getRobotsIndexAllow() { return robotsIndexAllow; }
    public void setRobotsIndexAllow(Boolean robotsIndexAllow) { this.robotsIndexAllow = robotsIndexAllow; }
}
