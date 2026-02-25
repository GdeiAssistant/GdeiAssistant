package cn.gdeiassistant.core.card.repository;

import cn.gdeiassistant.common.pojo.Document.CardTestDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CardDaoImpl implements CardDao {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

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

