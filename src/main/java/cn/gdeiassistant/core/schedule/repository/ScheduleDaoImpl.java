package cn.gdeiassistant.core.schedule.repository;

import cn.gdeiassistant.common.exception.customscheduleexception.CountOverLimitException;
import cn.gdeiassistant.common.exception.customscheduleexception.GenerateScheduleException;
import cn.gdeiassistant.common.pojo.document.CustomScheduleDocument;
import cn.gdeiassistant.common.pojo.document.ScheduleDocument;
import cn.gdeiassistant.common.pojo.entity.CustomSchedule;
import cn.gdeiassistant.common.pojo.entity.Schedule;
import cn.gdeiassistant.common.tools.utils.ScheduleUtils;
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
            org.bson.Document values = new org.bson.Document();
            mongoTemplate.getConverter().write(scheduleDocument, values);
            values.remove("_id");
            mongoTemplate.upsert(new Query(Criteria.where("username").is(scheduleDocument.getUsername())),
                    org.springframework.data.mongodb.core.query.Update.fromDocument(
                            new org.bson.Document("$set", values)), "schedule");
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
            var documents = mongoTemplate.find(new Query(Criteria.where("username").is(username)).limit(2),
                    CustomScheduleDocument.class, "customSchedule");
            if (documents.size() > 1) throw new IllegalStateException("自定义课表存在重复用户记录，需要核对后再更新");
            return documents.isEmpty() ? null : documents.get(0);
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
    private static final int MAX_CUSTOM_COURSES_PER_POSITION = 5;
    private static final int MAX_UPDATE_ATTEMPTS = 8;

    @Override
    public void addCustomSchedule(String username, CustomSchedule customSchedule)
            throws CountOverLimitException, GenerateScheduleException {
        requireMongo();
        Schedule schedule = ScheduleUtils.generateCustomSchedule(customSchedule);
        for (int attempt = 0; attempt < MAX_UPDATE_ATTEMPTS; attempt++) {
            CustomScheduleDocument document = queryCustomSchedule(username);
            if (document == null) {
                document = new CustomScheduleDocument();
                document.setId(cn.gdeiassistant.common.tools.utils.StringEncryptUtils.sha256HexString("customSchedule:" + username));
                document.setUsername(username);
                document.setVersion(0L);
                document.setScheduleMap(new LinkedHashMap<>(Map.of(schedule.getId(), schedule)));
                try {
                    mongoTemplate.insert(document, "customSchedule");
                    return;
                } catch (org.springframework.dao.DuplicateKeyException concurrentInsert) {
                    continue;
                }
            }
            Map<String, Schedule> courses = new LinkedHashMap<>(document.getScheduleMap() == null ? Map.of() : document.getScheduleMap());
            long count = courses.values().stream().filter(course -> java.util.Objects.equals(course.getPosition(), schedule.getPosition())).count();
            if (count >= MAX_CUSTOM_COURSES_PER_POSITION) throw new CountOverLimitException("最多可以保存五个自定义课表");
            courses.put(schedule.getId(), schedule);
            if (replaceCourses(document, courses)) return;
        }
        throw new org.springframework.dao.ConcurrencyFailureException("课表正在更新，请重试");
    }

    @Override
    public boolean deleteCustomSchedule(String username, Integer position) {
        return deleteCustomSchedule(username, position, null);
    }

    @Override
    public boolean deleteCustomSchedule(String username, Integer position, String courseId) {
        requireMongo();
        for (int attempt = 0; attempt < MAX_UPDATE_ATTEMPTS; attempt++) {
            CustomScheduleDocument document = queryCustomSchedule(username);
            if (document == null || document.getScheduleMap() == null) return false;
            Map<String, Schedule> courses = new LinkedHashMap<>(document.getScheduleMap());
            var matches = courses.entrySet().stream().filter(entry ->
                    java.util.Objects.equals(position, entry.getValue().getPosition())
                    && (courseId == null || courseId.equals(entry.getKey()))).toList();
            if (matches.isEmpty()) return false;
            if (matches.size() != 1) throw new IllegalArgumentException("请选择需要删除的课程");
            courses.remove(matches.get(0).getKey());
            if (replaceCourses(document, courses)) return true;
        }
        throw new org.springframework.dao.ConcurrencyFailureException("课表正在更新，请重试");
    }

    private boolean replaceCourses(CustomScheduleDocument document, Map<String, Schedule> courses) {
        Query expected = new Query(Criteria.where("_id").is(document.getId()).and("version").is(document.getVersion()));
        var update = new org.springframework.data.mongodb.core.query.Update().set("scheduleMap", courses)
                .set("version", document.getVersion() == null ? 1L : document.getVersion() + 1);
        return mongoTemplate.updateFirst(expected, update, CustomScheduleDocument.class, "customSchedule").getModifiedCount() == 1;
    }

    private void requireMongo() {
        if (mongoTemplate == null) throw new cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException("课表存储未配置");
    }
}
