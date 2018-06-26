package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule implements Serializable {

    //课程在单元格中所在的位置
    private int position;

    //课程长度
    private int scheduleLength;

    //课程名称
    private String scheduleName;

    //课程类型
    private String scheduleType;

    //上课节数
    private String scheduleLesson;

    //任课教师
    private String scheduleTeacher;

    //上课地点
    private String scheduleLocation;

    //该单元格位置的背景颜色
    private String colorCode;

    //单元格所在的行,从0开始计算
    private int row;

    //单元格所在的列,从0开始计算
    private int column;

    //课程周数
    private String scheduleWeek;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getScheduleLength() {
        return scheduleLength;
    }

    public void setScheduleLength(int scheduleLength) {
        this.scheduleLength = scheduleLength;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getScheduleLesson() {
        return scheduleLesson;
    }

    public void setScheduleLesson(String scheduleLesson) {
        this.scheduleLesson = scheduleLesson;
    }

    public String getScheduleTeacher() {
        return scheduleTeacher;
    }

    public void setScheduleTeacher(String scheduleTeacher) {
        this.scheduleTeacher = scheduleTeacher;
    }

    public String getScheduleLocation() {
        return scheduleLocation;
    }

    public void setScheduleLocation(String scheduleLocation) {
        this.scheduleLocation = scheduleLocation;
    }

    public String getScheduleWeek() {
        return scheduleWeek;
    }

    public void setScheduleWeek(String scheduleWeek) {
        this.scheduleWeek = scheduleWeek;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
