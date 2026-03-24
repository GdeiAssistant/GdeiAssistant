package cn.gdeiassistant.core.bookquery.service;

import cn.gdeiassistant.common.exception.BookRenewException.BookRenewOvertimeException;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult;
import cn.gdeiassistant.common.pojo.Entity.Book;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.collectionquery.service.CollectionQueryService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.integration.library.LibraryClient;
import cn.gdeiassistant.integration.library.pojo.LibraryRenewResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BookQueryService {

    private final Logger logger = LoggerFactory.getLogger(BookQueryService.class);

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private CollectionQueryService collectionQueryService;

    @Autowired
    private LibraryClient libraryClient;

    /**
     * 馆藏检索。
     */
    public CollectionQueryResult searchCollections(String sessionId, Integer page, String keyword) throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
        CollectionQueryResult result = collectionQueryService.collectionQuery(page, keyword);
        if (result == null) {
            result = new CollectionQueryResult();
            result.setCollectionList(Collections.emptyList());
            result.setSumPage(0);
        }
        return result;
    }

    /**
     * 馆藏详情。
     */
    public CollectionDetail getCollectionDetail(String sessionId, String detailURL) throws NetWorkTimeoutException, ServerErrorException {
        return collectionQueryService.getCollectionDetailByDetailURL(detailURL);
    }

    /**
     * 查询我的借阅。
     */
    public List<Book> getBorrowedBooks(String sessionId, String password) throws NetWorkTimeoutException, ServerErrorException, PasswordIncorrectException {
        return bookquery(sessionId, password);
    }

    /**
     * 续借图书。
     */
    public void renewBook(String sessionId, String sn, String code) throws NetWorkTimeoutException, ServerErrorException, BookRenewOvertimeException {
        bookRenew(sessionId, sn, code);
    }

    /**
     * 续借图书（原爬虫逻辑）
     */
    public void bookRenew(String sessionId, String sn, String code) throws NetWorkTimeoutException, ServerErrorException, BookRenewOvertimeException {
        try {
            LibraryRenewResult result = libraryClient.renewBook(sessionId, sn, code);
            if (result.getResult() == 1) {
                if ("超过最大续借次数:1！".equals(result.getMessage())) {
                    throw new BookRenewOvertimeException("图书续借超过次数限制");
                }
                return;
            }
            throw new ServerErrorException("图书馆系统异常");
        } catch (BookRenewOvertimeException e) {
            logger.error("续借图书异常:", e);
            throw new BookRenewOvertimeException("图书续借超过次数限制");
        } catch (java.io.IOException e) {
            logger.error("续借图书异常:", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("续借图书异常", e);
            throw new ServerErrorException("图书馆系统异常");
        }
    }

    /**
     * 查询借阅图书
     */
    public List<Book> bookquery(String sessionId, String password) throws NetWorkTimeoutException, ServerErrorException, PasswordIncorrectException {
        UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
        try {
            Document document = libraryClient.fetchBorrowedBooksPage(sessionId, userCertificate.getNumber(), password);
            Elements books = document.getElementsByClass("tableLib");
            List<Book> bookList = new ArrayList<>();
            for (Element element : books) {
                String[] renew = element.getElementsByClass("tableCon").first().select("a")
                        .first().attr("onclick").split("','");
                String sn = renew[0].split("renew\\('")[1];
                String code = renew[1].split("', '")[0];
                element.select("td").first().select("a").remove();
                String id = element.select("td").first().text();
                String name = element.select("td").get(1).text();
                String author = element.select("td").get(2).text();
                String borrowTime = element.select("td").get(3).text();
                String returnTime = element.select("td").get(4).text();
                Integer renewTime = Integer.valueOf(element.select("td").get(5).text());
                Book book = new Book();
                book.setId(id);
                book.setName(name);
                book.setAuthor(author);
                book.setBorrowDate(borrowTime);
                book.setReturnDate(returnTime);
                book.setRenewTime(renewTime);
                book.setSn(sn);
                book.setCode(code);
                bookList.add(book);
            }
            return bookList;
        } catch (PasswordIncorrectException e) {
            throw new PasswordIncorrectException("图书馆查询密码错误");
        } catch (java.io.IOException e) {
            logger.error("查询借阅图书异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("查询借阅图书异常：", e);
            throw new ServerErrorException("图书馆系统异常");
        }
    }
}
