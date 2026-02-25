package cn.gdeiassistant.core.book.repository;

import cn.gdeiassistant.common.pojo.Document.BookListDocument;
import cn.gdeiassistant.common.pojo.Entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class BookDaoImpl implements BookDao {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

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
