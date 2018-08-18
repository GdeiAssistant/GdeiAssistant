package com.linguancheng.gdeiassistant.Repository.Mongodb.Schedule;

import com.linguancheng.gdeiassistant.Pojo.Document.ScheduleDocument;

public interface ScheduleDao {

    public void saveSchedule(ScheduleDocument scheduleDocument);

    public ScheduleDocument queryScheduleByUsername(String username);

    public void removeSchedule(String username);

}
