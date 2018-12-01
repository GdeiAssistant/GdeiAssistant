package com.linguancheng.gdeiassistant.Repository.Mongodb.Schedule;

import com.linguancheng.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import com.linguancheng.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import com.linguancheng.gdeiassistant.Pojo.Document.CustomScheduleDocument;
import com.linguancheng.gdeiassistant.Pojo.Document.ScheduleDocument;
import com.linguancheng.gdeiassistant.Pojo.Entity.CustomSchedule;

public interface ScheduleDao {

    public void saveSchedule(ScheduleDocument scheduleDocument);

    public ScheduleDocument querySchedule(String username);

    public CustomScheduleDocument queryCustomSchedule(String username);

    public void removeSchedule(String username);

    public void addCustomSchedule(String username, CustomSchedule customSchedule) throws CountOverLimitException, GenerateScheduleException;

    public void deleteCustomSchedule(String username, String id);
}
