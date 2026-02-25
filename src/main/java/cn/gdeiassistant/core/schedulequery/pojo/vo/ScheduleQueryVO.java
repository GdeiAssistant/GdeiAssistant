package cn.gdeiassistant.core.schedulequery.pojo.vo;

import cn.gdeiassistant.common.pojo.Entity.Schedule;

import java.util.List;

/**
 * 课表查询结果 VO。
 */
public class ScheduleQueryVO {

    private List<Schedule> scheduleList;
    private Integer week;

    public List<Schedule> getScheduleList() { return scheduleList; }
    public void setScheduleList(List<Schedule> scheduleList) { this.scheduleList = scheduleList; }
    public Integer getWeek() { return week; }
    public void setWeek(Integer week) { this.week = week; }

    public ScheduleQueryVO() {}

    public ScheduleQueryVO(List<Schedule> scheduleList, int week) {
        this.scheduleList = scheduleList;
        this.week = week;
    }
}
