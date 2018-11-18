package com.linguancheng.gdeiassistant.Pojo.Document;

import com.linguancheng.gdeiassistant.Pojo.Entity.Grade;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document
public class GradeDocument {

    /**
     * 主键ID
     */
    @Id
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 成绩信息列表
     */
    private List<List<Grade>> gradeLists;

    /**
     * 第一学期GPA列表
     */
    private List<Double> firstTermGPAList;

    /**
     * 第一学期IGP列表
     */
    private List<Double> firstTermIGPList;

    /**
     * 第二学期GPA列表
     */
    private List<Double> secondTermGPAList;

    /**
     * 第二学期IGP列表
     */
    private List<Double> secondTermIGPList;

    /**
     * 更新时间
     */
    private Date updateDateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Double> getFirstTermGPAList() {
        return firstTermGPAList;
    }

    public void setFirstTermGPAList(List<Double> firstTermGPAList) {
        this.firstTermGPAList = firstTermGPAList;
    }

    public List<Double> getFirstTermIGPList() {
        return firstTermIGPList;
    }

    public void setFirstTermIGPList(List<Double> firstTermIGPList) {
        this.firstTermIGPList = firstTermIGPList;
    }

    public List<Double> getSecondTermGPAList() {
        return secondTermGPAList;
    }

    public void setSecondTermGPAList(List<Double> secondTermGPAList) {
        this.secondTermGPAList = secondTermGPAList;
    }

    public List<Double> getSecondTermIGPList() {
        return secondTermIGPList;
    }

    public void setSecondTermIGPList(List<Double> secondTermIGPList) {
        this.secondTermIGPList = secondTermIGPList;
    }

    public Date getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(Date updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public List<List<Grade>> getGradeList() {
        return gradeLists;
    }

    public void setGradeList(List<List<Grade>> gradeLists) {
        this.gradeLists = gradeLists;
    }
}
