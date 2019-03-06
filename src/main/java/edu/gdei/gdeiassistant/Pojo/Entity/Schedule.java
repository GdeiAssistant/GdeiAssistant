package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule implements Serializable {

    //课程编号
    private String id;

    //课程长度
    private Integer scheduleLength;

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

    /**
     * 单元格位置Position、行Row、列Columm的关系
     * Position = Row * 7 + Column
     * Row = Position / 7
     * Column = Position % 7
     */

    //课程在单元格中所在的位置
    //顺序从上到下，从左到右，数值从0开始计算
    private Integer position;

    //单元格所在的行,从0开始计算
    //数值也代表课程的节数
    //第n节：n = row + 1
    private Integer row;

    //单元格所在的列,从0开始计算
    //数值也代表星期几
    //星期几：n = colum + 1
    private Integer column;

    //最小的课程周数
    private Integer minScheduleWeek;

    //最大的课程周数
    private Integer maxScheduleWeek;

    //课程周数
    private String scheduleWeek;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getScheduleLength() {
        return scheduleLength;
    }

    public void setScheduleLength(Integer scheduleLength) {
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

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMaxScheduleWeek() {
        return maxScheduleWeek;
    }

    public void setMaxScheduleWeek(Integer maxScheduleWeek) {
        this.maxScheduleWeek = maxScheduleWeek;
    }

    public Integer getMinScheduleWeek() {
        return minScheduleWeek;
    }

    public void setMinScheduleWeek(Integer minScheduleWeek) {
        this.minScheduleWeek = minScheduleWeek;
    }

    public String getScheduleWeek() {
        return scheduleWeek;
    }

    public void setScheduleWeek(String scheduleWeek) {
        this.scheduleWeek = scheduleWeek;
    }
}
