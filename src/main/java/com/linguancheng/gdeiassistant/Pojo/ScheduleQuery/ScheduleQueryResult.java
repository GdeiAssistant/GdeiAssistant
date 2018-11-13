package com.linguancheng.gdeiassistant.Pojo.ScheduleQuery;

import com.linguancheng.gdeiassistant.Pojo.Entity.Schedule;

import java.util.List;

public class ScheduleQueryResult {

    private List<Schedule> scheduleList;

    private Integer week;

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public ScheduleQueryResult() {
    }

    public ScheduleQueryResult(List<Schedule> scheduleList, int week) {
        this.scheduleList = scheduleList;
        this.week = week;
    }
}
