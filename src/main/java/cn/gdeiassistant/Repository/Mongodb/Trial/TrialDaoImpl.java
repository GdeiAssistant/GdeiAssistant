package cn.gdeiassistant.Repository.Mongodb.Trial;

import cn.gdeiassistant.Pojo.Document.TrialDocument;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class TrialDaoImpl implements TrialDao {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    /**
     * 查询模拟查询数据信息
     *
     * @param type
     * @return
     */
    @Override
    public TrialDocument queryTrialData(String type) {
        if (mongoTemplate != null) {
            return mongoTemplate.findOne(new Query(Criteria.where("type").is(type))
                    , TrialDocument.class, "trial");
        }
        return null;
    }

    /**
     * 查询模拟查询数据信息
     *
     * @param type
     * @return
     */
    @Override
    public TrialDocument queryTrialData(String type, int index) {
        if (mongoTemplate != null) {
            return mongoTemplate.find(new Query(Criteria.where("type").is(type.toLowerCase()))
                    , TrialDocument.class, "trial").get(index);
        }
        return null;
    }
}
