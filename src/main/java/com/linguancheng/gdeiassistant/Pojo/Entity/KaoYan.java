package com.linguancheng.gdeiassistant.Pojo.Entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
public class KaoYan implements Serializable {

    private String name;

    private String signUpNumber;

    private String examNumber;

    private String totalScore;

    private String firstScore;

    private String secondScore;

    private String thirdScore;

    private String fourthScore;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignUpNumber() {
        return signUpNumber;
    }

    public void setSignUpNumber(String signUpNumber) {
        this.signUpNumber = signUpNumber;
    }

    public String getExamNumber() {
        return examNumber;
    }

    public void setExamNumber(String examNumber) {
        this.examNumber = examNumber;
    }

    public String getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(String totalScore) {
        this.totalScore = totalScore;
    }

    public String getFirstScore() {
        return firstScore;
    }

    public void setFirstScore(String firstScore) {
        this.firstScore = firstScore;
    }

    public String getSecondScore() {
        return secondScore;
    }

    public void setSecondScore(String secondScore) {
        this.secondScore = secondScore;
    }

    public String getThirdScore() {
        return thirdScore;
    }

    public void setThirdScore(String thirdScore) {
        this.thirdScore = thirdScore;
    }

    public String getFourthScore() {
        return fourthScore;
    }

    public void setFourthScore(String fourthScore) {
        this.fourthScore = fourthScore;
    }
}
