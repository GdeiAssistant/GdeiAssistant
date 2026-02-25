package cn.gdeiassistant.core.spareroom.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.Entity;

import java.io.Serializable;

/**
 * 空课室查询结果（非 DB 表，来自教务系统爬取）。与 common.SpareRoom 字段一致。
 */
public class SpareRoomEntity implements Serializable, Entity {

    private String number;
    private String name;
    private String type;
    private String zone;
    private String classSeating;
    private String section;
    private String examSeating;

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public String getClassSeating() { return classSeating; }
    public void setClassSeating(String classSeating) { this.classSeating = classSeating; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getExamSeating() { return examSeating; }
    public void setExamSeating(String examSeating) { this.examSeating = examSeating; }
}
