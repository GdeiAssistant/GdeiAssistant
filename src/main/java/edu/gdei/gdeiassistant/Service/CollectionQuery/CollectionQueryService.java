package edu.gdei.gdeiassistant.Service.CollectionQuery;

import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import edu.gdei.gdeiassistant.Pojo.CollectionQuery.CollectionDetailQuery;
import edu.gdei.gdeiassistant.Pojo.CollectionQuery.CollectionQueryResult;
import edu.gdei.gdeiassistant.Pojo.Entity.Collection;
import edu.gdei.gdeiassistant.Pojo.Entity.CollectionDetail;
import edu.gdei.gdeiassistant.Pojo.Entity.CollectionDistribution;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CollectionQueryService {

    private Logger logger = LoggerFactory.getLogger(CollectionQueryService.class);

    /**
     * 查询馆藏信息
     *
     * @param page
     * @param keyword
     * @return
     */
    public CollectionQueryResult CollectionQuery(Integer page, String keyword) throws NetWorkTimeoutException, ErrorQueryConditionException, ServerErrorException {
        CollectionQueryResult collectionQueryResult = new CollectionQueryResult();
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().
                    connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).
                    writeTimeout(10, TimeUnit.SECONDS).build();
            //查询图书列表
            String url = "http://agentdockingopac.featurelib.libsou.com/showhome/searchlist/opacSearchList?search=" + keyword
                    + "&xc=3&schoolId=705&centerDomain=&searchtype=title";
            if (page != 1) {
                url = url + "&page=" + page;
            }
            Request request = new Request.Builder().url(url).build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                //解析HTML页面
                Document document = Jsoup.parse(response.body().string());
                Integer currentPage = 1;
                Integer sumPage = null;
                Element pagenum = document.getElementById("pagenum");
                if (pagenum == null) {
                    //没有搜索结果
                    return null;
                }
                sumPage = Integer.valueOf(pagenum.select("option").first().text().split("/")[1]);
                if (currentPage > sumPage) {
                    //已经超过总页数
                    throw new ErrorQueryConditionException("查询页数超过总页数");
                }
                collectionQueryResult.setSumPage(sumPage);
                //馆藏图书列表
                List<Collection> collectionList = new ArrayList<>();
                Elements list = document.getElementsByClass("list");
                for (Element collectionLis : list.select("li")) {
                    Collection collection = new Collection();
                    collection.setDetailURL(collectionLis.select("a").attr("href").split("\\?")[1]);
                    collection.setBookname(collectionLis.getElementsByClass("title").first()
                            .select("span").first().text().split(" ")[1]);
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
                //关闭响应
                response.close();
                //返回查询结果
                collectionQueryResult.setCollectionList(collectionList);
                return collectionQueryResult;
            }
            throw new ServerErrorException("移动图书馆系统异常");
        } catch (IOException e) {
            logger.error("查询馆藏图书异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (ErrorQueryConditionException e) {
            logger.error("查询馆藏图书异常：", e);
            throw e;
        } catch (Exception e) {
            logger.error("查询馆藏图书异常：", e);
            throw new ServerErrorException("图书馆系统异常");
        }
    }

    /**
     * 查询图书详细信息
     *
     * @param collectionDetailQuery
     * @return
     */
    public CollectionDetail CollectionDetailQuery(CollectionDetailQuery collectionDetailQuery) throws NetWorkTimeoutException, ServerErrorException {
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().
                    connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).
                    writeTimeout(10, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().url("http://agentdockingopac.featurelib.libsou.com/showhome/searchdetail" +
                    "/opacSearchDetail?opacUrl=" + collectionDetailQuery.getOpacUrl() + "&search=" + collectionDetailQuery.getSearch()
                    + "&schoolId=" + collectionDetailQuery.getSchoolId() + "&searchtype=" + collectionDetailQuery.getSearchtype() + "&page="
                    + collectionDetailQuery.getPage() + "&xc=" + collectionDetailQuery.getXc()).build();
            Response response = okHttpClient.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());
            if (response.isSuccessful()) {
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
            }
            throw new ServerErrorException("移动图书馆系统异常");
        } catch (IOException e) {
            logger.error("查询馆藏图书详细信息异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("查询馆藏图书详细信息异常：", e);
            throw new ServerErrorException("图书馆系统异常");
        }
    }
}
