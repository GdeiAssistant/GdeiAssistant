package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YiBanUser implements Serializable {

    @SerializedName("yb_userid")
    private String userID;

    @SerializedName("yb_username")
    private String username;

    @SerializedName("yb_usernick")
    private String kickname;

    @SerializedName("yb_sex")
    private String gender;

    @SerializedName("yb_money")
    private String money;

    @SerializedName("yb_exp")
    private String experience;

    @SerializedName("yb_userhead")
    private String avatar;

    @SerializedName("yb_schoolid")
    private String schoolCode;

    @SerializedName("yb_schoolname")
    private String schoolName;

    @SerializedName("yb_regtime")
    private String registerTime;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKickname() {
        return kickname;
    }

    public void setKickname(String kickname) {
        this.kickname = kickname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime;
    }
}
