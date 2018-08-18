package com.gdeiassistant.gdeiassistant.Repository.Mongodb.Schedule;

import com.gdeiassistant.gdeiassistant.Pojo.Document.ScheduleDocument;

public interface ScheduleDao {

    public void saveSchedule(ScheduleDocument scheduleDocument);

    public ScheduleDocument queryScheduleByUsername(String username);

    public void removeSchedule(String username);

}
