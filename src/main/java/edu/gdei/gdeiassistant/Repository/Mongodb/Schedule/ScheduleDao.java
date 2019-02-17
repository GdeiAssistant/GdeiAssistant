package edu.gdei.gdeiassistant.Repository.Mongodb.Schedule;

import edu.gdei.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import edu.gdei.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import edu.gdei.gdeiassistant.Pojo.Document.CustomScheduleDocument;
import edu.gdei.gdeiassistant.Pojo.Document.ScheduleDocument;
import edu.gdei.gdeiassistant.Pojo.Entity.CustomSchedule;

public interface ScheduleDao {

    public void saveSchedule(ScheduleDocument scheduleDocument);

    public ScheduleDocument querySchedule(String username);

    public CustomScheduleDocument queryCustomSchedule(String username);

    public void removeSchedule(String username);

    public void addCustomSchedule(String username, CustomSchedule customSchedule) throws CountOverLimitException, GenerateScheduleException;

    public void deleteCustomSchedule(String username, String id);
}
