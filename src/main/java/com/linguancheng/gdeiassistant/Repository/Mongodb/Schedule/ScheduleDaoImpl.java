package com.linguancheng.gdeiassistant.Repository.Mongodb.Schedule;

import com.linguancheng.gdeiassistant.Pojo.Document.ScheduleDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class ScheduleDaoImpl implements ScheduleDao {

    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 保存课表信息
     *
     * @param scheduleDocument
     */
    @Override
    public void saveSchedule(ScheduleDocument scheduleDocument) {
        mongoTemplate.save(scheduleDocument, "schedule");
    }

    /**
     * 查询用户的课表信息
     *
     * @param username
     * @return
     */
    @Override
    public ScheduleDocument queryScheduleByUsername(String username) {
        return mongoTemplate.findOne(new Query(Criteria.where("username").is(username))
                , ScheduleDocument.class, "schedule");
    }
}
