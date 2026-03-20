package cn.gdeiassistant.core.collectionquery.service;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionDetailQuery;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult;
import cn.gdeiassistant.common.pojo.Entity.Collection;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import cn.gdeiassistant.common.pojo.Entity.CollectionDistribution;
import cn.gdeiassistant.integration.library.LibraryClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollectionQueryService {

    private final Logger logger = LoggerFactory.getLogger(CollectionQueryService.class);

    @Autowired
    private LibraryClient libraryClient;

    /**
     * 查询馆藏信息
     */
    public CollectionQueryResult collectionQuery(Integer page, String keyword) throws NetWorkTimeoutException, ErrorQueryConditionException, ServerErrorException {
        CollectionQueryResult collectionQueryResult = new CollectionQueryResult();
        try {
            Document document = libraryClient.fetchCollectionListPage(page, keyword);
            Integer currentPage = 1;
            Integer sumPage = null;
            Element pagenum = document.getElementById("pagenum");
            if (pagenum == null) {
                return null;
            }
            sumPage = Integer.valueOf(pagenum.select("option").first().text().split("/")[1]);
            if (currentPage > sumPage) {
                throw new ErrorQueryConditionException("查询页数超过总页数");
            }
            collectionQueryResult.setSumPage(sumPage);
            List<Collection> collectionList = new ArrayList<>();
            Elements list = document.getElementsByClass("list");
            for (Element collectionLis : list.select("li")) {
                Collection collection = new Collection();
                collection.setDetailURL(collectionLis.select("a").attr("href").split("\\?")[1]);
                collection.setBookname(collectionLis.getElementsByClass("title").first()
                        .select("span").first().text().split(" ")[1]);
                Elements details = collectionLis.getElementsByClass("detail")
                        .first().select("p");
                if (details.size() != 0) {
                    if (details.size() == 1) {
                        collection.setPublishingHouse(details.get(0).text());
                    } else if (details.size() == 2) {
                        collection.setAuthor(details.get(0).text());
                        collection.setPublishingHouse(details.get(1).text());
                    }
                }
                collectionList.add(collection);
            }
            collectionQueryResult.setCollectionList(collectionList);
            return collectionQueryResult;
        } catch (ErrorQueryConditionException e) {
            logger.error("查询馆藏图书异常：", e);
            throw e;
        } catch (Exception e) {
            logger.error("查询馆藏图书异常：", e);
            throw new ServerErrorException("图书馆系统异常");
        }
    }

    /**
     * 根据 detailURL（查询字符串）查询馆藏详情。用于 GET /api/library/detail。
     */
    public CollectionDetail getCollectionDetailByDetailURL(String detailURL) throws NetWorkTimeoutException, ServerErrorException {
        if (detailURL == null || detailURL.trim().isEmpty()) {
            return null;
        }
        CollectionDetailQuery q = parseDetailURL(detailURL.trim());
        return q != null ? getCollectionDetailByQuery(q) : null;
    }

    private static CollectionDetailQuery parseDetailURL(String detailURL) {
        Map<String, String> params = new HashMap<>();
        try {
            for (String pair : detailURL.split("&")) {
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8.name());
                    params.put(key, value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        String opacUrl = params.get("opacUrl");
        String search = params.get("search");
        String schoolId = params.get("schoolId");
        String searchtype = params.get("searchtype");
        String page = params.get("page");
        String xc = params.get("xc");
        if (opacUrl == null || search == null) {
            return null;
        }
        CollectionDetailQuery q = new CollectionDetailQuery();
        q.setOpacUrl(opacUrl);
        q.setSearch(search);
        q.setSchoolId(schoolId != null ? schoolId : "705");
        q.setSearchtype(searchtype != null ? searchtype : "title");
        q.setPage(page != null ? page : "1");
        q.setXc(xc != null ? xc : "3");
        return q;
    }

    /**
     * 查询图书详细信息
     */
    public CollectionDetail getCollectionDetailByQuery(CollectionDetailQuery collectionDetailQuery) throws NetWorkTimeoutException, ServerErrorException {
        try {
            Document document = libraryClient.fetchCollectionDetailPage(
                    collectionDetailQuery.getOpacUrl(),
                    collectionDetailQuery.getSearch(),
                    collectionDetailQuery.getSchoolId(),
                    collectionDetailQuery.getSearchtype(),
                    collectionDetailQuery.getPage(),
                    collectionDetailQuery.getXc());
            CollectionDetail collectionDetail = new CollectionDetail();
            Element tit = document.getElementsByClass("tit").first();
            if (tit == null) {
                return null;
            }
            Element catalog = document.getElementsByClass("catalog").first();
            Elements tableLib = document.getElementsByClass("tableLib");
            collectionDetail.setBookname(tit.select("h1").text());
            collectionDetail.setAuthor(tit.select("p").text().split("：")[1]);
            collectionDetail.setPrincipal(catalog.select("p").first().text().split("：")[1]);
            collectionDetail.setPublishingHouse(catalog.select("p").get(1).text().split("：")[1]);
            collectionDetail.setPrice(catalog.select("p").get(2).text().split("：")[1]);
            collectionDetail.setPhysicalDescriptionArea(catalog.select("p").get(3).text().split("：")[1]);
            collectionDetail.setPersonalPrincipal(catalog.select("p").get(4).text().split("：")[1]);
            collectionDetail.setSubjectTheme(catalog.select("p").get(5).text().split("：")[1]);
            collectionDetail.setChineseLibraryClassification(catalog.select("p").get(6).text().split("：")[1]);
            List<CollectionDistribution> collectionDistributionList = new ArrayList<>();
            for (Element distribution : tableLib) {
                CollectionDistribution collectionDistribution = new CollectionDistribution();
                collectionDistribution.setBarcode(distribution.select("td").first().text());
                collectionDistribution.setCallNumber(distribution.select("td").get(1).text());
                collectionDistribution.setLocation(distribution.select("td").get(2).text());
                collectionDistribution.setState(distribution.select("td").get(3).text());
                collectionDistributionList.add(collectionDistribution);
            }
            collectionDetail.setCollectionDistributionList(collectionDistributionList);
            return collectionDetail;
        } catch (Exception e) {
            logger.error("查询馆藏图书详细信息异常：", e);
            throw new ServerErrorException("图书馆系统异常");
        }
    }
}
