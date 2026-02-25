package cn.gdeiassistant.core.collection.repository;

import cn.gdeiassistant.common.pojo.Document.CollectionDetailEntry;
import cn.gdeiassistant.common.pojo.Document.CollectionTestDocument;
import cn.gdeiassistant.common.pojo.Entity.Collection;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class CollectionDaoImpl implements CollectionDao {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Override
    public List<Collection> queryCollectionListByUsername(String username) {
        if (mongoTemplate == null || username == null) {
            return Collections.emptyList();
        }
        CollectionTestDocument doc = mongoTemplate.findOne(
                new Query(Criteria.where("username").is(username)),
                CollectionTestDocument.class,
                "collection"
        );
        if (doc == null || doc.getCollectionList() == null) {
            return Collections.emptyList();
        }
        return doc.getCollectionList();
    }

    @Override
    public CollectionDetail queryCollectionDetailByDetailURL(String username, String detailURL) {
        if (mongoTemplate == null || username == null || detailURL == null || detailURL.isEmpty()) {
            return null;
        }
        CollectionTestDocument doc = mongoTemplate.findOne(
                new Query(Criteria.where("username").is(username)),
                CollectionTestDocument.class,
                "collection"
        );
        if (doc == null || doc.getCollectionDetailList() == null) {
            return null;
        }
        for (CollectionDetailEntry entry : doc.getCollectionDetailList()) {
            if (detailURL.equals(entry.getDetailURL()) && entry.getDetail() != null) {
                return entry.getDetail();
            }
        }
        return null;
    }
}
