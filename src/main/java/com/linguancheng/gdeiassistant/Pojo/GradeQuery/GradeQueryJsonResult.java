package com.linguancheng.gdeiassistant.Pojo.GradeQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.linguancheng.gdeiassistant.Pojo.Entity.Grade;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties
public class GradeQueryJsonResult {

    private Integer queryYear;

    private Double firstTermGPA;

    private Double firstTermIGP;

    private Double secondTermGPA;

    private Double secondTermIGP;

    private List<Grade> firstTermGradeList;

    private List<Grade> secondTermGradeList;

    private boolean success;

    private boolean empty;

    private String errorMessage;

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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
