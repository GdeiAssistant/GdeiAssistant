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
    private String grade_year;
    //课程学期
    private String grade_term;
    //课程编号
    private String grade_id;
    //课程名称
    private String grade_name;
    //课程学分
    private String grade_credit;
    //课程类型
    private String grade_type;
    //课程绩点
    private String grade_gpa;
    //课程成绩
    private String grade_score;

    public String getGrade_year() {
        return grade_year;
    }

    public void setGrade_year(String grade_year) {
        this.grade_year = grade_year;
    }

    public String getGrade_term() {
        return grade_term;
    }

    public void setGrade_term(String grade_term) {
        this.grade_term = grade_term;
    }

    public String getGrade_id() {
        return grade_id;
    }

    public void setGrade_id(String grade_id) {
        this.grade_id = grade_id;
    }

    public String getGrade_name() {
        return grade_name;
    }

    public void setGrade_name(String grade_name) {
        this.grade_name = grade_name;
    }

    public String getGrade_credit() {
        return grade_credit;
    }

    public void setGrade_credit(String grade_credit) {
        this.grade_credit = grade_credit;
    }

    public String getGrade_type() {
        return grade_type;
    }

    public void setGrade_type(String grade_type) {
        this.grade_type = grade_type;
    }

    public String getGrade_gpa() {
        return grade_gpa;
    }

    public void setGrade_gpa(String grade_gpa) {
        this.grade_gpa = grade_gpa;
    }

    public String getGrade_score() {
        return grade_score;
    }

    public void setGrade_score(String grade_score) {
        this.grade_score = grade_score;
    }
}
