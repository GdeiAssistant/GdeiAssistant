package cn.gdeiassistant.core.profile.pojo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * profile 表持久化映射，@Result 列名与库表一致。
 */
public class ProfileEntity implements Serializable {

    private String username;
    private String nickname;
    private Date birthday;
    private Integer degree;
    private Integer faculty;
    private String major;
    private Integer enrollment;
    private Integer profession;
    private String colleges;
    private String highSchool;
    private String juniorHighSchool;
    private String primarySchool;
    private String locationRegion;
    private String locationState;
    private String locationCity;
    private String hometownRegion;
    private String hometownState;
    private String hometownCity;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Date getBirthday() { return birthday; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public Integer getDegree() { return degree; }
    public void setDegree(Integer degree) { this.degree = degree; }
    public Integer getFaculty() { return faculty; }
    public void setFaculty(Integer faculty) { this.faculty = faculty; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Integer getEnrollment() { return enrollment; }
    public void setEnrollment(Integer enrollment) { this.enrollment = enrollment; }
    public Integer getProfession() { return profession; }
    public void setProfession(Integer profession) { this.profession = profession; }
    public String getColleges() { return colleges; }
    public void setColleges(String colleges) { this.colleges = colleges; }
    public String getHighSchool() { return highSchool; }
    public void setHighSchool(String highSchool) { this.highSchool = highSchool; }
    public String getJuniorHighSchool() { return juniorHighSchool; }
    public void setJuniorHighSchool(String juniorHighSchool) { this.juniorHighSchool = juniorHighSchool; }
    public String getPrimarySchool() { return primarySchool; }
    public void setPrimarySchool(String primarySchool) { this.primarySchool = primarySchool; }
    public String getLocationRegion() { return locationRegion; }
    public void setLocationRegion(String locationRegion) { this.locationRegion = locationRegion; }
    public String getLocationState() { return locationState; }
    public void setLocationState(String locationState) { this.locationState = locationState; }
    public String getLocationCity() { return locationCity; }
    public void setLocationCity(String locationCity) { this.locationCity = locationCity; }
    public String getHometownRegion() { return hometownRegion; }
    public void setHometownRegion(String hometownRegion) { this.hometownRegion = hometownRegion; }
    public String getHometownState() { return hometownState; }
    public void setHometownState(String hometownState) { this.hometownState = hometownState; }
    public String getHometownCity() { return hometownCity; }
    public void setHometownCity(String hometownCity) { this.hometownCity = hometownCity; }
}
