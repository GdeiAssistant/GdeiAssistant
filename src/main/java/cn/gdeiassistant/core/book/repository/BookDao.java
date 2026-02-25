package cn.gdeiassistant.core.book.repository;

import cn.gdeiassistant.common.pojo.Entity.Book;

import java.util.List;

/**
 * MongoDB 集合 "book"：按用户名查询借阅列表（测试账号用）。
 */
public interface BookDao {

    /**
     * 按用户名查询 bookList，无则返回 null。
     */
    List<Book> queryBookListByUsername(String username);
}
