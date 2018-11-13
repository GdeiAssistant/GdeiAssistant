package com.gdeiassistant.gdeiassistant.Pojo.ScheduleQuery;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.Schedule;
import com.gdeiassistant.gdeiassistant.Pojo.Result.JsonResult;

import java.util.List;

public class ScheduleQueryJsonResult extends JsonResult {

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
}
