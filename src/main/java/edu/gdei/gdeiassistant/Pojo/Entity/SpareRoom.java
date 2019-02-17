package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpareRoom implements Serializable {

    //教室编号
    private String number;

    //教室名称
    private String name;

    //教室类别
    private String type;

    //校区
    private String zone;

    //上课座位数
    private String classSeating;

    //使用部门
    private String section;

    //考试座位数
    private String examSeating;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getClassSeating() {
        return classSeating;
    }

    public void setClassSeating(String classSeating) {
        this.classSeating = classSeating;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getExamSeating() {
        return examSeating;
    }

    public void setExamSeating(String examSeating) {
        this.examSeating = examSeating;
    }
}
