package com.gdeiassistant.gdeiassistant.Repository.Mongodb.Schedule;

import com.gdeiassistant.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import com.gdeiassistant.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import com.gdeiassistant.gdeiassistant.Pojo.Document.CustomScheduleDocument;
import com.gdeiassistant.gdeiassistant.Pojo.Document.ScheduleDocument;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.CustomSchedule;

public interface ScheduleDao {

    public void saveSchedule(ScheduleDocument scheduleDocument);

    public ScheduleDocument querySchedule(String username);

    public CustomScheduleDocument queryCustomSchedule(String username);

    public void removeSchedule(String username);

    public void addCustomSchedule(String username, CustomSchedule customSchedule) throws CountOverLimitException, GenerateScheduleException;

    public void deleteCustomSchedule(String username, String id);
}
