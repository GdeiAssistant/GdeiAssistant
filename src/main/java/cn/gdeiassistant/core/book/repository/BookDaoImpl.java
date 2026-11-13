package cn.gdeiassistant.core.book.repository;

import cn.gdeiassistant.common.pojo.document.BookListDocument;
import cn.gdeiassistant.common.pojo.entity.Book;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class BookDaoImpl implements BookDao {

    private final MongoTemplate mongoTemplate;

    /** Direct injection for tests that already resolved Mongo (or null). */
    public BookDaoImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    public BookDaoImpl(ObjectProvider<MongoTemplate> mongoTemplate) {
        this(mongoTemplate.getIfAvailable());
    }

    @Override
    public List<Book> queryBookListByUsername(String username) {
        if (mongoTemplate == null || username == null) {
            return Collections.emptyList();
        }
        BookListDocument doc = mongoTemplate.findOne(
                new Query(Criteria.where("username").is(username)),
                BookListDocument.class,
                "book"
        );
        if (doc == null || doc.getBookList() == null) {
            return Collections.emptyList();
        }
        return doc.getBookList();
    }
}
