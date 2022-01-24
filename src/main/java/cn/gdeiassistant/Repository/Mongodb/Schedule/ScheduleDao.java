package cn.gdeiassistant.Repository.Mongodb.Schedule;

import cn.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.Exception.DatasourceException.MongodbNotConfiguredException;
import cn.gdeiassistant.Pojo.Document.CustomScheduleDocument;
import cn.gdeiassistant.Pojo.Document.ScheduleDocument;
import cn.gdeiassistant.Pojo.Entity.CustomSchedule;

public interface ScheduleDao {

    public void saveSchedule(ScheduleDocument scheduleDocument) throws MongodbNotConfiguredException;

    public ScheduleDocument querySchedule(String username);

    public CustomScheduleDocument queryCustomSchedule(String username);

    public void removeSchedule(String username) throws MongodbNotConfiguredException;

    public void addCustomSchedule(String username, CustomSchedule customSchedule) throws CountOverLimitException, GenerateScheduleException, MongodbNotConfiguredException;

    public void deleteCustomSchedule(String username, String id) throws MongodbNotConfiguredException;
}
