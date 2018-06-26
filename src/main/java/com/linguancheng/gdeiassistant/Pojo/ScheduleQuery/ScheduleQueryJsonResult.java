package com.linguancheng.gdeiassistant.Pojo.ScheduleQuery;

import com.linguancheng.gdeiassistant.Pojo.Entity.Schedule;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class ScheduleQueryJsonResult {

    private List<Schedule> scheduleList;

    private int selectedWeek;

    private boolean success;

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
