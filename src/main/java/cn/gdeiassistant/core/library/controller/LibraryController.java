package cn.gdeiassistant.core.library.controller;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.common.pojo.Entity.Book;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.bookquery.service.BookQueryService;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult;
import cn.gdeiassistant.core.collectionquery.service.CollectionQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    @Autowired
    private CollectionQueryService collectionQueryService;

    @Autowired
    private BookQueryService bookQueryService;

    /**
     * 图书馆检索。GET /api/library/search?keyword=xxx&page=1
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public DataJsonResult<CollectionQueryResult> search(HttpServletRequest request,
                                                        @RequestParam("keyword") String keyword,
                                                        @RequestParam(value = "page", defaultValue = "1") Integer page)
            throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
        if (page == null || page < 1) {
            page = 1;
        }
        CollectionQueryResult result = collectionQueryService.collectionQuery(page, keyword);
        if (result == null || result.getCollectionList() == null) {
            result = new CollectionQueryResult();
            result.setSumPage(0);
            result.setCollectionList(java.util.Collections.emptyList());
        }
        return new DataJsonResult<>(true, result);
    }

    /**
     * 图书馆详情。GET /api/library/detail?detailURL=xxx
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public DataJsonResult<CollectionDetail> detail(HttpServletRequest request,
                                                   @RequestParam("detailURL") String detailURL)
            throws NetWorkTimeoutException, ServerErrorException {
        CollectionDetail detail = collectionQueryService.getCollectionDetailByDetailURL(detailURL);
        return new DataJsonResult<>(true, detail);
    }

    /**
     * 我的借阅。GET /api/library/borrow?password=xxx
     */
    @RequestMapping(value = "/borrow", method = RequestMethod.GET)
    public DataJsonResult<List<Book>> getBorrow(HttpServletRequest request,
                                                @RequestParam(value = "password", required = false) String password) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        String pwd = (password != null && !password.isEmpty()) ? password : "";
        List<Book> list = bookQueryService.getBorrowedBooks(sessionId, pwd);
        return new DataJsonResult<>(true, list);
    }

    /**
     * 图书续借。POST /api/library/renew，body: { sn, code, password }
     */
    @RequestMapping(value = "/renew", method = RequestMethod.POST)
    public JsonResult renew(HttpServletRequest request, @RequestBody LibraryRenewRequest body) throws Exception {
        String sn = body != null ? body.getSn() : null;
        String code = body != null ? body.getCode() : null;
        String password = body != null ? body.getPassword() : null;
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

    public static class LibraryRenewRequest {
        private String sn;
        private String code;
        private String password;

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
