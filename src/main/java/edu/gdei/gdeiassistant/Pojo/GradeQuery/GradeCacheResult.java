package edu.gdei.gdeiassistant.Pojo.GradeQuery;

import edu.gdei.gdeiassistant.Pojo.Entity.Grade;

import java.util.List;

public class GradeCacheResult {

    private Double[] firstTermGPAArray;

    private Double[] secondTermGPAArray;

    private Double[] firstTermIGPArray;

    private Double[] secondTermIGPArray;

    private List<Grade>[] gradeListArray;

    public Double[] getFirstTermGPAArray() {
        return firstTermGPAArray;
    }

    public void setFirstTermGPAArray(Double[] firstTermGPAArray) {
        this.firstTermGPAArray = firstTermGPAArray;
    }

    public Double[] getSecondTermGPAArray() {
        return secondTermGPAArray;
    }

    public void setSecondTermGPAArray(Double[] secondTermGPAArray) {
        this.secondTermGPAArray = secondTermGPAArray;
    }

    public Double[] getFirstTermIGPArray() {
        return firstTermIGPArray;
    }

    public void setFirstTermIGPArray(Double[] firstTermIGPArray) {
        this.firstTermIGPArray = firstTermIGPArray;
    }

    public Double[] getSecondTermIGPArray() {
        return secondTermIGPArray;
    }

    public void setSecondTermIGPArray(Double[] secondTermIGPArray) {
        this.secondTermIGPArray = secondTermIGPArray;
    }

    public List<Grade>[] getGradeListArray() {
        return gradeListArray;
    }

    public void setGradeListArray(List<Grade>[] gradeListArray) {
        this.gradeListArray = gradeListArray;
    }
}
