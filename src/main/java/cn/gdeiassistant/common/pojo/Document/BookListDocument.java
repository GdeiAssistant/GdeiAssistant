package cn.gdeiassistant.common.pojo.Document;

import cn.gdeiassistant.common.pojo.Entity.Book;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB 集合 "book"：测试账号我的借阅模拟数据。
 * 文档字段：username, bookList
 */
@Document(collection = "book")
public class BookListDocument {

    @Id
    private String id;

    private String username;

    private List<Book> bookList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void setBookList(List<Book> bookList) {
        this.bookList = bookList;
    }
}
