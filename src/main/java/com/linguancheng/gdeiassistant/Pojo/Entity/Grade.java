package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Created by linguancheng on 2017/7/22.
 */

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Grade implements Serializable {

    //课程学年
    private String gradeYear;

    //课程学期
    private String gradeTerm;

    //课程编号
    private String gradeId;

    //课程名称
    private String gradeName;

    //课程学分
    private String gradeCredit;

    //课程类型
    private String gradeType;

    //课程绩点
    private String gradeGpa;

    //课程成绩
    private String gradeScore;

    public String getGradeYear() {
        return gradeYear;
    }

    public void setGradeYear(String gradeYear) {
        this.gradeYear = gradeYear;
    }

    public String getGradeTerm() {
        return gradeTerm;
    }

    public void setGradeTerm(String gradeTerm) {
        this.gradeTerm = gradeTerm;
    }

    public String getGradeId() {
        return gradeId;
    }

    public void setGradeId(String gradeId) {
        this.gradeId = gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getGradeCredit() {
        return gradeCredit;
    }

    public void setGradeCredit(String gradeCredit) {
        this.gradeCredit = gradeCredit;
    }

    public String getGradeType() {
        return gradeType;
    }

    public void setGradeType(String gradeType) {
        this.gradeType = gradeType;
    }

    public String getGradeGpa() {
        return gradeGpa;
    }

    public void setGradeGpa(String gradeGpa) {
        this.gradeGpa = gradeGpa;
    }

    public String getGradeScore() {
        return gradeScore;
    }

    public void setGradeScore(String gradeScore) {
        this.gradeScore = gradeScore;
    }
}
