package cn.gdeiassistant.core.gradequery.pojo.dto;

import cn.gdeiassistant.common.pojo.Entity.Grade;

import java.io.Serializable;
import java.util.List;

/**
 * 成绩查询条件/数据结构 DTO（与教务返回结构一致）。
 */
public class GradeQueryDTO implements Serializable {

    private double firstTermGPA;
    private double firstTermIGP;
    private double secondTermGPA;
    private double secondTermIGP;
    private List<Grade> firstTermGradeList;
    private List<Grade> secondTermGradeList;

    public double getFirstTermGPA() { return firstTermGPA; }
    public void setFirstTermGPA(double firstTermGPA) { this.firstTermGPA = firstTermGPA; }
    public double getFirstTermIGP() { return firstTermIGP; }
    public void setFirstTermIGP(double firstTermIGP) { this.firstTermIGP = firstTermIGP; }
    public double getSecondTermGPA() { return secondTermGPA; }
    public void setSecondTermGPA(double secondTermGPA) { this.secondTermGPA = secondTermGPA; }
    public double getSecondTermIGP() { return secondTermIGP; }
    public void setSecondTermIGP(double secondTermIGP) { this.secondTermIGP = secondTermIGP; }
    public List<Grade> getFirstTermGradeList() { return firstTermGradeList; }
    public void setFirstTermGradeList(List<Grade> firstTermGradeList) { this.firstTermGradeList = firstTermGradeList; }
    public List<Grade> getSecondTermGradeList() { return secondTermGradeList; }
    public void setSecondTermGradeList(List<Grade> secondTermGradeList) { this.secondTermGradeList = secondTermGradeList; }
}
