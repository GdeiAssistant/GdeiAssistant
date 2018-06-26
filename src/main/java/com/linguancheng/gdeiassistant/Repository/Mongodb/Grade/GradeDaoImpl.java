package com.linguancheng.gdeiassistant.Repository.Mongodb.Grade;

import com.linguancheng.gdeiassistant.Pojo.Document.GradeDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class GradeDaoImpl implements GradeDao {

    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 保存用户成绩信息
     *
     * @param gradeDocument
     */
    @Override
    public void saveGrade(GradeDocument gradeDocument) {
        mongoTemplate.save(gradeDocument, "grade");
    }

    /**
     * 查询用户成绩信息
     *
     * @param username
     * @return
     */
    @Override
    public GradeDocument queryGradeByUsername(String username) {
        return mongoTemplate.findOne(new Query(Criteria.where("username").is(username))
                , GradeDocument.class, "grade");
    }
}
