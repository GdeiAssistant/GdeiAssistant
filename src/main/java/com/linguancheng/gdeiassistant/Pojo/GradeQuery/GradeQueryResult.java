package com.linguancheng.gdeiassistant.Pojo.GradeQuery;

import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.Grade;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by linguancheng on 2017/7/22.
 */

@Component
@Scope("prototype")
public class GradeQueryResult {

    private ServiceResultEnum gradeServiceResultEnum;

    private Integer queryYear;

    private Double firstTermGPA;

    private Double firstTermIGP;

    private Double secondTermGPA;

    private Double secondTermIGP;

    private List<Grade> firstTermGradeList;

    private List<Grade> secondTermGradeList;

    public ServiceResultEnum getGradeServiceResultEnum() {
        return gradeServiceResultEnum;
    }

    public void setGradeServiceResultEnum(ServiceResultEnum gradeServiceResultEnum) {
        this.gradeServiceResultEnum = gradeServiceResultEnum;
    }

    public Integer getQueryYear() {
        return queryYear;
    }

    public void setQueryYear(Integer queryYear) {
        this.queryYear = queryYear;
    }

    public Double getFirstTermGPA() {
        return firstTermGPA;
    }

    public void setFirstTermGPA(Double firstTermGPA) {
        this.firstTermGPA = firstTermGPA;
    }

    public Double getFirstTermIGP() {
        return firstTermIGP;
    }

    public void setFirstTermIGP(Double firstTermIGP) {
        this.firstTermIGP = firstTermIGP;
    }

    public Double getSecondTermGPA() {
        return secondTermGPA;
    }

    public void setSecondTermGPA(Double secondTermGPA) {
        this.secondTermGPA = secondTermGPA;
    }

    public Double getSecondTermIGP() {
        return secondTermIGP;
    }

    public void setSecondTermIGP(Double secondTermIGP) {
        this.secondTermIGP = secondTermIGP;
    }

    public List<Grade> getFirstTermGradeList() {
        return firstTermGradeList;
    }

    public void setFirstTermGradeList(List<Grade> firstTermGradeList) {
        this.firstTermGradeList = firstTermGradeList;
    }

    public List<Grade> getSecondTermGradeList() {
        return secondTermGradeList;
    }

    public void setSecondTermGradeList(List<Grade> secondTermGradeList) {
        this.secondTermGradeList = secondTermGradeList;
    }
}
