package cn.gdeiassistant.Repository.Mongodb.Schedule;

import cn.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.Pojo.Document.CustomScheduleDocument;
import cn.gdeiassistant.Pojo.Document.ScheduleDocument;
import cn.gdeiassistant.Pojo.Entity.CustomSchedule;
import cn.gdeiassistant.Pojo.Entity.Schedule;
import cn.gdeiassistant.Tools.Utils.ScheduleUtils;
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
            Schedule schedule = ScheduleUtils.GenerateCustomSchedule(customSchedule);
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
     * 删除自定义课程信息
     *
     * @param username
     * @param id
     */
    @Override
    public void deleteCustomSchedule(String username, String id) {
        if (mongoTemplate != null) {
            //生成课程编号
            CustomScheduleDocument customScheduleDocument = queryCustomSchedule(username);
            if (customScheduleDocument != null) {
                customScheduleDocument.getScheduleMap().remove(id);
                mongoTemplate.save(customScheduleDocument, "customSchedule");
            }
        }
    }
}
