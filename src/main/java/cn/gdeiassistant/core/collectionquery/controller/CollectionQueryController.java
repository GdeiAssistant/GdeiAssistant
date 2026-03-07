package cn.gdeiassistant.core.collectionquery.controller;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionDetailQuery;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionQuery;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult;
import cn.gdeiassistant.common.pojo.Entity.Book;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.bookquery.service.BookQueryService;
import cn.gdeiassistant.core.collectionquery.service.CollectionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CollectionQueryController {

    @Autowired
    private CollectionQueryService collectionQueryService;

    @Autowired
    private BookQueryService bookQueryService;

    /**
     * 馆藏全局检索（公共接口）
     * GET /api/collection/search?keyword=xxx&page=1
     * 测试账号由 TrialDataAspect 从 MongoDB collection 集合返回，严禁穿透爬虫。
     */
    @TrialData(value = "collection")
    @RequestMapping(value = "/api/collection/search", method = RequestMethod.GET)
    public DataJsonResult<CollectionQueryResult> search(HttpServletRequest request,
                                                        @RequestParam("keyword") String keyword,
                                                        @RequestParam(value = "page", defaultValue = "1") Integer page) throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
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
     * 馆藏详情。GET /api/collection/detail?detailURL=xxx
     * 测试账号由 TrialDataAspect 从 MongoDB collection 集合返回，严禁穿透爬虫。
     */
    @TrialData(value = "collection")
    @RequestMapping(value = "/api/collection/detail", method = RequestMethod.GET)
    public DataJsonResult<CollectionDetail> detail(HttpServletRequest request,
                                                   @RequestParam("detailURL") String detailURL) throws NetWorkTimeoutException, ServerErrorException {
        CollectionDetail d = collectionQueryService.getCollectionDetailByDetailURL(detailURL);
        return new DataJsonResult<>(true, d);
    }

    /**
     * 查询我的借阅。测试账号从 MongoDB trial 集合返回；正常账号需传图书馆密码。
     * GET /api/collection/borrow?password=xxx（测试账号可省略 password）
     */
    @RequestMapping(value = "/api/collection/borrow", method = RequestMethod.GET)
    public DataJsonResult<List<Book>> getBorrow(HttpServletRequest request,
                                                @RequestParam(value = "password", required = false) String password) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        String pwd = (password != null && !password.isEmpty()) ? password : "";
        List<Book> list = bookQueryService.getBorrowedBooks(sessionId, pwd);
        return new DataJsonResult<>(true, list);
    }

    /**
     * 图书续借。测试账号抛出 TestAccountException。
     * POST /api/collection/renew，参数 sn、code
     */
    @RequestMapping(value = "/api/collection/renew", method = RequestMethod.POST)
    public JsonResult renew(HttpServletRequest request,
                            @RequestParam("sn") String sn,
                            @RequestParam("code") String code) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        bookQueryService.renewBook(sessionId, sn, code);
        return new JsonResult(true);
    }

    /**
     * 馆藏图书详细信息查询接口
     */
    @RequestMapping(value = "/api/collection/detail/query", method = RequestMethod.POST)
    public DataJsonResult<CollectionDetail> restCollectionDetailQuery(@Validated CollectionDetailQuery collectionDetailQuery) throws ServerErrorException, NetWorkTimeoutException {
        CollectionDetail collectionDetail = collectionQueryService.getCollectionDetailByQuery(collectionDetailQuery);
        return new DataJsonResult<>(true, collectionDetail);
    }

    /**
     * 馆藏信息查询接口
     */
    @RequestMapping(value = "/api/collection/query", method = RequestMethod.POST)
    public DataJsonResult<CollectionQueryResult> restfulCollectionQuery(@Validated CollectionQuery collectionQuery) throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
        CollectionQueryResult collectionQueryResult = collectionQueryService.collectionQuery(collectionQuery.getPage(), collectionQuery.getBookname());
        if (collectionQueryResult == null || collectionQueryResult.getCollectionList().isEmpty()) {
            return new DataJsonResult<>(new JsonResult(false, "查询结果为空"));
        }
        return new DataJsonResult<>(true, collectionQueryResult);
    }
}
