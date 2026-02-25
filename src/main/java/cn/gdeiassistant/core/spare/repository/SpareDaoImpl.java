package cn.gdeiassistant.core.spare.repository;

import cn.gdeiassistant.common.pojo.Document.SpareTestDocument;
import cn.gdeiassistant.common.pojo.Entity.SpareRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class SpareDaoImpl implements SpareDao {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Override
    public List<SpareRoom> querySpareListByUsername(String username) {
        if (mongoTemplate == null || username == null) {
            return Collections.emptyList();
        }
        SpareTestDocument doc = mongoTemplate.findOne(
                new Query(Criteria.where("username").is(username)),
                SpareTestDocument.class,
                "spare"
        );
        if (doc == null || doc.getSpareList() == null) {
            return Collections.emptyList();
        }
        return doc.getSpareList();
    }
}
