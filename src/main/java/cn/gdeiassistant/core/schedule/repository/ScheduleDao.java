package cn.gdeiassistant.core.schedule.repository;

import cn.gdeiassistant.common.exception.customscheduleexception.CountOverLimitException;
import cn.gdeiassistant.common.exception.customscheduleexception.GenerateScheduleException;
import cn.gdeiassistant.common.pojo.document.CustomScheduleDocument;
import cn.gdeiassistant.common.pojo.document.ScheduleDocument;
import cn.gdeiassistant.common.pojo.entity.CustomSchedule;

public interface ScheduleDao {

    void saveSchedule(ScheduleDocument scheduleDocument);

    ScheduleDocument querySchedule(String username);

    CustomScheduleDocument queryCustomSchedule(String username);

    void removeSchedule(String username);

    void addCustomSchedule(String username, CustomSchedule customSchedule) throws CountOverLimitException, GenerateScheduleException;

    /**
     * 删除当前用户自定义课表中指定 position 的自定义课程。
     *
     * @param username 当前用户名校验归属
     * @param position 自定义课程在 custom_schedule 中的 position
     * @return true 表示已删除，false 表示该 position 不属于当前用户的自定义课程（越权或非自定义）
     */
    boolean deleteCustomSchedule(String username, Integer position);

    boolean deleteCustomSchedule(String username, Integer position, String courseId);
}
