package cn.gdeiassistant.core.spareRoom.pojo.vo;

import java.io.Serializable;

/**
 * 空课室查询结果视图。
 */
public class SpareRoomVO implements Serializable {

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
