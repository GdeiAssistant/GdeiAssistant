package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Entity.Collection;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.collection.repository.CollectionDao;
import cn.gdeiassistant.core.trailData.service.TrialDataService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 馆藏与图书检索样板间处理器：collection、booksearch、bookdetail。
 */
@Component
public class CollectionTrialHandler extends AbstractTrialModuleHandler {

    @Autowired(required = false)
    private CollectionDao collectionDao;

    @Override
    public String getModuleKey() {
        return "collection";
    }

    @Override
    public java.util.List<String> getSupportedModules() {
        return java.util.Arrays.asList("collection", "booksearch", "bookdetail");
    }

    @Override
    public DataJsonResult<?> handle(String sessionId,
                                    HttpServletRequest request,
                                    TrialData trialData,
                                    ProceedingJoinPoint joinPoint) {
        String module = trialData.value();
        cn.gdeiassistant.common.pojo.Entity.User user =
                (cn.gdeiassistant.common.pojo.Entity.User) request.getAttribute("user");
        String username = user != null ? user.getUsername() : null;

        switch (module) {
            case "collection":
                return handleCollection(request, username);
            case "booksearch":
                return handleBookSearch(request, username);
            case "bookdetail":
                return handleBookDetail(request, username);
            default:
                return null;
        }
    }

    private DataJsonResult<?> handleCollection(HttpServletRequest req, String username) {
        String detailURL = req.getParameter("detailURL");
        if (detailURL != null && !detailURL.isEmpty()) {
            if (collectionDao == null) {
                return new DataJsonResult<>(true, buildFallbackCollectionDetail(detailURL));
            }
            CollectionDetail detail = collectionDao.queryCollectionDetailByDetailURL(username, detailURL);
            if (detail == null) {
                detail = buildFallbackCollectionDetail(detailURL);
            }
            return new DataJsonResult<>(true, detail);
        }

        return loadFromTrialOrFallback("collection", () -> {
            if (collectionDao == null) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            List<Collection> fullList = collectionDao.queryCollectionListByUsername(username);
            if (fullList == null || fullList.isEmpty()) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            int page = parsePage(req.getParameter("page"), 1);
            int sumPage = fullList.isEmpty() ? 0 : Math.max(1, (fullList.size() + 9) / 10);
            int from = (page - 1) * 10;
            int to = Math.min(from + 10, fullList.size());
            List<Collection> list = from < fullList.size() ? fullList.subList(from, to) : Collections.emptyList();
            cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult result =
                    new cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult();
            result.setCollectionList(list);
            result.setSumPage(sumPage);
            return new DataJsonResult<>(true, result);
        });
    }

    private DataJsonResult<?> handleBookSearch(HttpServletRequest req, String username) {
        if (collectionDao == null) {
            return new DataJsonResult<>(true, buildSearchData(Collections.emptyList(), 0));
        }
        List<Collection> fullList = collectionDao.queryCollectionListByUsername(username);
        if (fullList == null) fullList = Collections.emptyList();
        int page = parsePage(req.getParameter("page"), 1);
        int sumPage = fullList.isEmpty() ? 0 : Math.max(1, (fullList.size() + 9) / 10);
        int from = (page - 1) * 10;
        int to = Math.min(from + 10, fullList.size());
        List<Collection> list = from < fullList.size() ? fullList.subList(from, to) : Collections.emptyList();
        return new DataJsonResult<>(true, buildSearchData(list, sumPage));
    }

    private DataJsonResult<?> handleBookDetail(HttpServletRequest req, String username) {
        String detailURLParam = req.getParameter("detailURL");
        // 优先从 trial 集合加载样板间详情数据（type = collection_detail）
        return loadFromTrialOrFallback("collection_detail", () -> {
            // 回退到旧的 collection 集合逻辑
            if (collectionDao == null) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            CollectionDetail detail = collectionDao.queryCollectionDetailByDetailURL(username, detailURLParam);
            if (detail == null) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            return new DataJsonResult<>(true, detail);
        });
    }

    private static Map<String, Object> buildSearchData(List<Collection> list, int sumPage) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("sumPage", sumPage);
        return data;
    }

    private static int parsePage(String pageStr, int defaultPage) {
        if (pageStr == null || pageStr.isEmpty()) return defaultPage;
        try {
            int p = Integer.parseInt(pageStr.trim());
            return p < 1 ? defaultPage : p;
        } catch (NumberFormatException e) {
            return defaultPage;
        }
    }

    private static CollectionDetail buildFallbackCollectionDetail(String detailURL) {
        CollectionDetail fallback = new CollectionDetail();
        fallback.setBookname("未找到该馆藏详情");
        fallback.setCollectionDistributionList(Collections.emptyList());
        return fallback;
    }
}

