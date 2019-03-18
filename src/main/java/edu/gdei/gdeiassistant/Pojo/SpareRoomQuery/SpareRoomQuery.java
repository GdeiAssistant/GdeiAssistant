package edu.gdei.gdeiassistant.Pojo.SpareRoomQuery;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class SpareRoomQuery implements Serializable {

    //校区
    @NotNull
    @Min(0)
    @Max(4)
    private Integer zone;

    //教室类别
    @NotNull
    @Min(0)
    @Max(52)
    private Integer type;

    //最小座位数
    @Min(0)
    private Integer minSeating;

    //最大座位数
    @Max(500)
    private Integer maxSeating;

    //起始时间
    @NotNull
    @Min(1)
    @Max(20)
    private Integer startTime;

    //结束时间
    @NotNull
    @Min(1)
    @Max(20)
    private Integer endTime;

    //最小星期数
    @NotNull
    @Min(0)
    @Max(6)
    private Integer minWeek;

    //最大星期数
    @NotNull
    @Min(0)
    @Max(6)
    private Integer maxWeek;

    //单双周
    @NotNull
    @Min(0)
    @Max(2)
    private Integer weekType;

    //节数
    @NotNull
    @Min(0)
    @Max(10)
    private Integer classNumber;

    public Integer getZone() {
        return zone;
    }

    public void setZone(Integer zone) {
        this.zone = zone;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getMinSeating() {
        return minSeating;
    }

    public void setMinSeating(Integer minSeating) {
        this.minSeating = minSeating;
    }

    public Integer getMaxSeating() {
        return maxSeating;
    }

    public void setMaxSeating(Integer maxSeating) {
        this.maxSeating = maxSeating;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }

    public Integer getMinWeek() {
        return minWeek;
    }

    public void setMinWeek(Integer minWeek) {
        this.minWeek = minWeek;
    }

    public Integer getMaxWeek() {
        return maxWeek;
    }

    public void setMaxWeek(Integer maxWeek) {
        this.maxWeek = maxWeek;
    }

    public Integer getWeekType() {
        return weekType;
    }

    public void setWeekType(Integer weekType) {
        this.weekType = weekType;
    }

    public Integer getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(Integer classNumber) {
        this.classNumber = classNumber;
    }
}
