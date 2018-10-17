package com.linguancheng.gdeiassistant.Pojo.ScheduleQuery;

import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.Schedule;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class ScheduleQueryResult {

    private ServiceResultEnum scheduleServiceResultEnum;

    private List<Schedule> scheduleList;

    private int selectedWeek;

    public ServiceResultEnum getScheduleServiceResultEnum() {
        return scheduleServiceResultEnum;
    }

    public void setScheduleServiceResultEnum(ServiceResultEnum scheduleServiceResultEnum) {
        this.scheduleServiceResultEnum = scheduleServiceResultEnum;
    }

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public int getSelectedWeek() {
        return selectedWeek;
    }

    public void setSelectedWeek(int selectedWeek) {
        this.selectedWeek = selectedWeek;
    }
}
