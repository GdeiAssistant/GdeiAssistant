package cn.gdeiassistant.core.schedulequery.pojo;

import cn.gdeiassistant.common.pojo.Entity.Schedule;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ScheduleQueryResult {

    @JsonProperty("scheduleList")
    private List<Schedule> scheduleList;

    @JsonProperty("week")
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
