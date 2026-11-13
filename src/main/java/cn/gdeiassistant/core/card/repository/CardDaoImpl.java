package cn.gdeiassistant.core.card.repository;

import cn.gdeiassistant.common.pojo.document.CardTestDocument;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CardDaoImpl implements CardDao {

    private final MongoTemplate mongoTemplate;

    /** Direct injection for tests that already resolved Mongo (or null). */
    public CardDaoImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    public CardDaoImpl(ObjectProvider<MongoTemplate> mongoTemplate) {
        this(mongoTemplate.getIfAvailable());
    }

    @Override
    public CardTestDocument queryCardDocumentByUsername(String username) {
        if (mongoTemplate == null || username == null) {
            return null;
        }
        return mongoTemplate.findOne(
                new Query(Criteria.where("username").is(username)),
                CardTestDocument.class,
                "card"
        );
    }
}

