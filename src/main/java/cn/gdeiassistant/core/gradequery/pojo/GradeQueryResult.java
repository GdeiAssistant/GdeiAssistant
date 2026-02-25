package cn.gdeiassistant.core.gradequery.pojo;

import cn.gdeiassistant.common.pojo.Entity.Grade;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GradeQueryResult {

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("firstTermGPA")
    private Double firstTermGPA;

    @JsonProperty("firstTermIGP")
    private Double firstTermIGP;

    @JsonProperty("secondTermGPA")
    private Double secondTermGPA;

    @JsonProperty("secondTermIGP")
    private Double secondTermIGP;

    @JsonProperty("firstTermGradeList")
    private List<Grade> firstTermGradeList;

    @JsonProperty("secondTermGradeList")
    private List<Grade> secondTermGradeList;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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
