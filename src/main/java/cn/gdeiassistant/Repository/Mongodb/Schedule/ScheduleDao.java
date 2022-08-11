package cn.gdeiassistant.Repository.Mongodb.Schedule;

import cn.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.Pojo.Document.CustomScheduleDocument;
import cn.gdeiassistant.Pojo.Document.ScheduleDocument;
import cn.gdeiassistant.Pojo.Entity.CustomSchedule;

public interface ScheduleDao {

    void saveSchedule(ScheduleDocument scheduleDocument);

    ScheduleDocument querySchedule(String username);

    CustomScheduleDocument queryCustomSchedule(String username);

    void removeSchedule(String username);

    void addCustomSchedule(String username, CustomSchedule customSchedule) throws CountOverLimitException, GenerateScheduleException;

    void deleteCustomSchedule(String username, String id) ;
}
