package cn.gdeiassistant.core.grade.repository;

import cn.gdeiassistant.common.pojo.document.GradeDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class GradeDaoImpl implements GradeDao {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    /**
     * 保存用户成绩信息
     *
     * @param gradeDocument
     */
    @Override
    public void saveGrade(GradeDocument gradeDocument) {
        if (mongoTemplate != null) {
            org.bson.Document values = new org.bson.Document();
            mongoTemplate.getConverter().write(gradeDocument, values);
            values.remove("_id");
            mongoTemplate.upsert(new Query(Criteria.where("username").is(gradeDocument.getUsername())),
                    org.springframework.data.mongodb.core.query.Update.fromDocument(
                            new org.bson.Document("$set", values)), "grade");
        }
    }

    /**
     * 查询用户成绩信息
     *
     * @param username
     * @return
     */
    @Override
    public GradeDocument queryGrade(String username) {
        if (mongoTemplate != null) {
            return mongoTemplate.findOne(new Query(Criteria.where("username").is(username))
                    , GradeDocument.class, "grade");
        }
        return null;
    }

    /**
     * 删除用户缓存的成绩信息
     *
     * @param username
     */
    @Override
    public void removeGrade(String username) {
        if (mongoTemplate != null) {
            mongoTemplate.remove(new Query(Criteria.where("username").is(username)), "grade");
        }
    }
}
