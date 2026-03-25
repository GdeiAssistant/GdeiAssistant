package cn.gdeiassistant.core.userProfile.pojo;

import java.io.Serializable;

/**
 * 前后端分离：当前用户信息接口 GET /api/user/profile 的返回数据结构。
 */
public class UserProfileVO implements Serializable {

    private String username;
    private String nickname;
    private String avatar;
    private ProfileFacultyValueVO faculty;
    private ProfileMajorValueVO major;
    private String enrollment;
    private ProfileLocationValueVO location;
    private ProfileLocationValueVO hometown;
    private String introduction;
    private String birthday;
    private String ipArea;
    private Integer age;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public ProfileFacultyValueVO getFaculty() { return faculty; }
    public void setFaculty(ProfileFacultyValueVO faculty) { this.faculty = faculty; }
    public ProfileMajorValueVO getMajor() { return major; }
    public void setMajor(ProfileMajorValueVO major) { this.major = major; }
    public String getEnrollment() { return enrollment; }
    public void setEnrollment(String enrollment) { this.enrollment = enrollment; }
    public ProfileLocationValueVO getLocation() { return location; }
    public void setLocation(ProfileLocationValueVO location) { this.location = location; }
    public ProfileLocationValueVO getHometown() { return hometown; }
    public void setHometown(ProfileLocationValueVO hometown) { this.hometown = hometown; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getIpArea() { return ipArea; }
    public void setIpArea(String ipArea) { this.ipArea = ipArea; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
