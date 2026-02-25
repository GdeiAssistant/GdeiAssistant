package cn.gdeiassistant.core.privacy.pojo.dto;

import java.io.Serializable;

/**
 * 更新隐私设置入参（与 Privacy 布尔字段对应）。
 */
public class PrivacyUpdateDTO implements Serializable {

    private Boolean facultyOpen;
    private Boolean majorOpen;
    private Boolean locationOpen;
    private Boolean hometownOpen;
    private Boolean introductionOpen;
    private Boolean enrollmentOpen;
    private Boolean ageOpen;
    private Boolean cacheAllow;
    private Boolean robotsIndexAllow;

    public Boolean getFacultyOpen() { return facultyOpen; }
    public void setFacultyOpen(Boolean facultyOpen) { this.facultyOpen = facultyOpen; }
    public Boolean getMajorOpen() { return majorOpen; }
    public void setMajorOpen(Boolean majorOpen) { this.majorOpen = majorOpen; }
    public Boolean getLocationOpen() { return locationOpen; }
    public void setLocationOpen(Boolean locationOpen) { this.locationOpen = locationOpen; }
    public Boolean getHometownOpen() { return hometownOpen; }
    public void setHometownOpen(Boolean hometownOpen) { this.hometownOpen = hometownOpen; }
    public Boolean getIntroductionOpen() { return introductionOpen; }
    public void setIntroductionOpen(Boolean introductionOpen) { this.introductionOpen = introductionOpen; }
    public Boolean getEnrollmentOpen() { return enrollmentOpen; }
    public void setEnrollmentOpen(Boolean enrollmentOpen) { this.enrollmentOpen = enrollmentOpen; }
    public Boolean getAgeOpen() { return ageOpen; }
    public void setAgeOpen(Boolean ageOpen) { this.ageOpen = ageOpen; }
    public Boolean getCacheAllow() { return cacheAllow; }
    public void setCacheAllow(Boolean cacheAllow) { this.cacheAllow = cacheAllow; }
    public Boolean getRobotsIndexAllow() { return robotsIndexAllow; }
    public void setRobotsIndexAllow(Boolean robotsIndexAllow) { this.robotsIndexAllow = robotsIndexAllow; }
}
