package cn.gdeiassistant.Repository.Mongodb.Grade;

import cn.gdeiassistant.Exception.DatasourceException.MongodbNotConfiguredException;
import cn.gdeiassistant.Pojo.Document.GradeDocument;
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
    public void saveGrade(GradeDocument gradeDocument) throws MongodbNotConfiguredException {
        if (mongoTemplate != null) {
            mongoTemplate.save(gradeDocument, "grade");
        }
        throw new MongodbNotConfiguredException("MongoDB数据源未配置");
    }

    /**
     * 查询用户成绩信息
     *
     * @param username
     * @return
     */
    @Override
    public GradeDocument queryGradeByUsername(String username) {
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
    public void removeGrade(String username) throws MongodbNotConfiguredException {
        if (mongoTemplate != null) {
            mongoTemplate.remove(new Query(Criteria.where("username").is(username)), "grade");
        }
        throw new MongodbNotConfiguredException("MongoDB数据源未配置");
    }
}
