package cn.gdeiassistant.core.bookquery.controller;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.pojo.Entity.Book;
import cn.gdeiassistant.common.pojo.Entity.Collection;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.bookquery.service.BookQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BookQueryController {

    @Autowired
    private BookQueryService bookQueryService;

    /**
     * 馆藏检索。GET /api/book/search?keyword=xxx&page=1
     * 测试账号由 TrialDataAspect 从 MongoDB collection 集合返回；正常账号走爬虫。
     * 返回 data: { list: List&lt;Collection&gt;, sumPage: Integer }
     */
    @TrialData(value = "booksearch")
    @RequestMapping(value = "/api/book/search", method = RequestMethod.GET)
    public DataJsonResult<Map<String, Object>> search(HttpServletRequest request,
                                                      @RequestParam("keyword") String keyword,
                                                      @RequestParam(value = "page", defaultValue = "1") Integer page) throws Exception {
        if (page == null || page < 1) page = 1;
        String sessionId = (String) request.getAttribute("sessionId");
        cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult result = bookQueryService.searchCollections(sessionId, page, keyword);
        List<Collection> list = (result != null && result.getCollectionList() != null) ? result.getCollectionList() : Collections.emptyList();
        Integer sumPage = (result != null && result.getSumPage() != null) ? result.getSumPage() : 0;
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("sumPage", sumPage);
        return new DataJsonResult<>(true, data);
    }

    /**
     * 馆藏详情。GET /api/book/detail?detailURL=xxx
     * 测试账号由 TrialDataAspect 从 MongoDB collection 集合返回；正常账号走爬虫。
     */
    @TrialData(value = "bookdetail")
    @RequestMapping(value = "/api/book/detail", method = RequestMethod.GET)
    public DataJsonResult<CollectionDetail> detail(HttpServletRequest request,
                                                   @RequestParam("detailURL") String detailURL) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        CollectionDetail detail = bookQueryService.getCollectionDetail(sessionId, detailURL);
        return new DataJsonResult<>(true, detail);
    }

    /**
     * 我的借阅。GET /api/book/borrow
     * 测试账号由 TrialDataAspect 从 MongoDB book 集合返回，严禁穿透爬虫。
     */
    @TrialData(value = "book")
    @RequestMapping(value = "/api/book/borrow", method = RequestMethod.GET)
    public DataJsonResult<List<Book>> borrow(HttpServletRequest request,
                                             @RequestParam(value = "password", required = false) String password) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        String pwd = (password != null && !password.isEmpty()) ? password : "";
        List<Book> list = bookQueryService.getBorrowedBooks(sessionId, pwd);
        return new DataJsonResult<>(true, list);
    }

    /**
     * 续借。POST /api/book/renew，body 含 sn, code, password。密码用于高危二次核验。
     * 测试账号由 TrialDataAspect 抛 TestAccountException，严禁穿透。
     */
    @TrialData(value = "bookrenew")
    @RequestMapping(value = "/api/book/renew", method = RequestMethod.POST)
    public JsonResult renew(HttpServletRequest request, @RequestBody Map<String, String> body) throws Exception {
        String sn = body != null ? body.get("sn") : null;
        String code = body != null ? body.get("code") : null;
        String password = body != null ? body.get("password") : null;
        if (sn == null || sn.isEmpty() || code == null || code.isEmpty()) {
            return new JsonResult(false, "缺少 sn 或 code");
        }
        if (password == null || password.isEmpty()) {
            return new JsonResult(false, "请提供图书馆密码");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        bookQueryService.bookquery(sessionId, password);
        bookQueryService.renewBook(sessionId, sn, code);
        return new JsonResult(true);
    }
}
