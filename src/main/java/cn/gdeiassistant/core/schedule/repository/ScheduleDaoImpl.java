package cn.gdeiassistant.core.schedule.repository;

import cn.gdeiassistant.common.exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.common.exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.common.pojo.Document.CustomScheduleDocument;
import cn.gdeiassistant.common.pojo.Document.ScheduleDocument;
import cn.gdeiassistant.common.pojo.Entity.CustomSchedule;
import cn.gdeiassistant.common.pojo.Entity.Schedule;
import cn.gdeiassistant.common.tools.Utils.ScheduleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class ScheduleDaoImpl implements ScheduleDao {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    /**
     * 查询用户的课表信息
     *
     * @param username
     * @return
     */
    @Override
    public ScheduleDocument querySchedule(String username) {
        if (mongoTemplate != null) {
            return mongoTemplate.findOne(new Query(Criteria.where("username").is(username))
                    , ScheduleDocument.class, "schedule");
        }
        return null;
    }

    /**
     * 保存课表信息
     *
     * @param scheduleDocument
     */
    @Override
    public void saveSchedule(ScheduleDocument scheduleDocument){
        if (mongoTemplate != null) {
            mongoTemplate.save(scheduleDocument, "schedule");
        }
    }

    /**
     * 删除用户缓存的课表信息
     *
     * @param username
     */
    @Override
    public void removeSchedule(String username) {
        if (mongoTemplate != null) {
            mongoTemplate.remove(new Query(Criteria.where("username").is(username)), "schedule");
        }
    }

    /**
     * 查询用户自定义的课程信息
     *
     * @param username
     * @return
     */
    @Override
    public CustomScheduleDocument queryCustomSchedule(String username) {
        if (mongoTemplate != null) {
            return mongoTemplate.findOne(new Query(Criteria.where("username").is(username))
                    , CustomScheduleDocument.class, "customSchedule");
        }
        return null;
    }

    /**
     * 添加自定义课表信息
     *
     * @param username
     * @param customSchedule
     * @throws CountOverLimitException
     * @throws GenerateScheduleException
     */
    @Override
    public synchronized void addCustomSchedule(String username, CustomSchedule customSchedule)
            throws CountOverLimitException, GenerateScheduleException {
        if (mongoTemplate != null) {
            //生成课程编号
            Schedule schedule = ScheduleUtils.generateCustomSchedule(customSchedule);
            //若数据库中已有自定义课表记录，则直接更新，否则进行添加
            CustomScheduleDocument customScheduleDocument = queryCustomSchedule(username);
            if (customScheduleDocument == null) {
                Map<String, Schedule> scheduleMap = new LinkedHashMap<>();
                scheduleMap.put(schedule.getId(), schedule);
                customScheduleDocument = new CustomScheduleDocument();
                customScheduleDocument.setUsername(username);
                customScheduleDocument.setScheduleMap(scheduleMap);
            } else {
                Map<Integer, Integer> positionCounter = new HashMap<>();
                Map<String, Schedule> scheduleMap = customScheduleDocument.getScheduleMap();
                for (Schedule s : scheduleMap.values()) {
                    positionCounter.put(s.getPosition(), positionCounter.getOrDefault(s.getPosition(), 0) + 1);
                }
                if (positionCounter.getOrDefault(schedule.getPosition(), 0) > 5) {
                    throw new CountOverLimitException("最多可以保存五个自定义课表");
                }
            }
            customScheduleDocument.getScheduleMap().put(schedule.getId(), schedule);
            mongoTemplate.save(customScheduleDocument, "customSchedule");
        }
    }

    /**
     * 删除自定义课程信息（按 position 定位，与 CustomSchedule 无 id 字段一致）。
     * 仅当该 position 确属当前用户 custom_schedule 中的自定义课程时才执行删除。
     *
     * @param username 当前用户名校验归属
     * @param position 自定义课程 position
     * @return true 已删除，false 该 position 非当前用户自定义课程（不执行删除）
     */
    @Override
    public boolean deleteCustomSchedule(String username, Integer position) {
        if (mongoTemplate == null || position == null) {
            return false;
        }
        CustomScheduleDocument customScheduleDocument = queryCustomSchedule(username);
        if (customScheduleDocument == null || customScheduleDocument.getScheduleMap() == null) {
            return false;
        }
        String keyToRemove = null;
        for (Map.Entry<String, Schedule> entry : customScheduleDocument.getScheduleMap().entrySet()) {
            if (position.equals(entry.getValue().getPosition())) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove == null) {
            return false;
        }
        customScheduleDocument.getScheduleMap().remove(keyToRemove);
        mongoTemplate.save(customScheduleDocument, "customSchedule");
        return true;
    }
}
