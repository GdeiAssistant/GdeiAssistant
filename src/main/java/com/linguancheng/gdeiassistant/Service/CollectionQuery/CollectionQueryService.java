package com.linguancheng.gdeiassistant.Service.CollectionQuery;

import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import com.linguancheng.gdeiassistant.Pojo.CollectionQuery.CollectionDetailQueryResult;
import com.linguancheng.gdeiassistant.Pojo.CollectionQuery.CollectionQueryResult;
import com.linguancheng.gdeiassistant.Pojo.Entity.Collection;
import com.linguancheng.gdeiassistant.Pojo.Entity.CollectionDetail;
import com.linguancheng.gdeiassistant.Pojo.Entity.CollectionDistribution;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CollectionQueryService {

    private Log log = LogFactory.getLog(CollectionQueryService.class);

    /**
     * 查询馆藏信息
     *
     * @param page
     * @param keyword
     * @return
     */
    public CollectionQueryResult CollectionQuery(int page, String keyword) {
        ApplicationContext context = new ClassPathXmlApplicationContext("config/spring/applicationContext.xml");
        CollectionQueryResult collectionQueryResult = (CollectionQueryResult) context.getBean("collectionQueryResult");
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().
                    connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS).
                    writeTimeout(5, TimeUnit.SECONDS).build();
            //查询图书列表
            String url = "http://lib2.gdei.edu.cn:8080/search?kw=" + keyword + "&searchtype=anywords";
            if (page != 1) {
                url = url + "&page=" + page;
            }
            Request request = new Request.Builder().url(url).build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                //解析HTML页面
                Document document = Jsoup.parse(response.body().string());
                String resultNumber = document.getElementsByClass("wrap").first().text();
                String s1[] = resultNumber.split(" ");
                String s2[] = s1[1].split("条");
                //获取图书搜索结果数
                int sum = Integer.valueOf(s2[0]);
                //计算页数
                int sumPage = 0;
                if (sum % 20 == 0) {
                    sumPage = sum / 20;
                } else {
                    sumPage = (sum / 20) + 1;
                }
                collectionQueryResult.setSumPage(sumPage);
                List<Collection> collectionList = new ArrayList<>();
                if (sum == 0) {
                    throw new ErrorQueryConditionException("查询结果为空");
                } else {
                    Elements li = document.select("li");
                    for (Element element : li) {
                        Element a = element.select("a").first();
                        String detailURL = a.attr("href");
                        String str0[] = detailURL.split("%3fctrlno%");
                        String str1[] = str0[1].split("&kw=");
                        detailURL = str1[0];
                        String bookname = a.text();
                        String divContent = element.select("div").first().text();
                        String s3[] = divContent.split(" ");
                        for (int j = 0; j < s3.length; j++) {
                            s3[j] = s3[j].trim();
                        }
                        String s4[] = s3[0].split("：");
                        String bookinfo = null;
                        if (s4.length > 1) {
                            bookinfo = s4[1] + " " + s3[1] + " " + s3[2];
                        } else {
                            bookinfo = s3[1] + " " + s3[2];
                        }
                        String s5[] = s3[4].split("：");
                        int sumCount = Integer.valueOf(s5[1].trim());
                        String s6[] = s3[6].split("：");
                        int availableCount = Integer.valueOf(s6[1].trim());
                        Collection collection = new Collection();
                        collection.setBookname(bookname);
                        collection.setBookinfo(bookinfo);
                        collection.setSumCount(sumCount);
                        collection.setAvailableCount(availableCount);
                        collection.setDetailURL(detailURL);
                        collectionList.add(collection);
                    }
                }
                //关闭响应
                response.close();
                //返回查询结果
                collectionQueryResult.setCollectionList(collectionList);
                collectionQueryResult.setCollectionQueryResultEnum(ServiceResultEnum.SUCCESS);
                return collectionQueryResult;
            }
            throw new ServerErrorException("移动图书馆系统异常");
        } catch (IOException e) {
            log.error("查询馆藏图书异常：" + e);
            collectionQueryResult.setCollectionQueryResultEnum(ServiceResultEnum.TIME_OUT);
        } catch (ErrorQueryConditionException e) {
            log.error("查询馆藏图书异常：" + e);
            collectionQueryResult.setCollectionQueryResultEnum(ServiceResultEnum.EMPTY_RESULT);
        } catch (Exception e) {
            log.error("查询馆藏图书异常：" + e);
            collectionQueryResult.setCollectionQueryResultEnum(ServiceResultEnum.SERVER_ERROR);
        }
        return collectionQueryResult;
    }

    /**
     * 查询图书详细信息
     *
     * @param url
     * @return
     */
    public CollectionDetailQueryResult CollectionDetailQuery(String url) {
        ApplicationContext context = new ClassPathXmlApplicationContext("config/spring/applicationContext.xml");
        CollectionDetailQueryResult collectionDetailQueryResult = (CollectionDetailQueryResult) context.getBean("collectionDetailQueryResult");
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().
                    connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS).
                    writeTimeout(5, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().url("http://lib2.gdei.edu.cn:8080/search?d=http%3a%2f%2f210.38.64.115%3a81%2fbookinfo.aspx%3fctrlno%" + url).build();
            Response response = okHttpClient.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());
            if (response.isSuccessful() && document.title().isEmpty()) {
                String detailContent = document.getElementById("ctl00_ContentPlaceHolder1_bookcardinfolbl").text();
                List<CollectionDistribution> collectionDistributionList = new ArrayList<>();
                CollectionDetail collectionDetail = new CollectionDetail();
                Elements sheet = document.getElementsByClass("sheet");
                for (Element element : sheet) {
                    String location = element.select("tr").first().select("td").first().text();
                    String callNumber = element.select("tr").get(1).select("td").first().text();
                    String accessionNumber = element.select("tr").get(2).select("td").first().text();
                    String state = element.select("tr").get(3).select("td").first().text();
                    CollectionDistribution collectionDistribution = new CollectionDistribution();
                    collectionDistribution.setLocation(location);
                    collectionDistribution.setCallNumber(callNumber);
                    collectionDistribution.setAccessionNumber(accessionNumber);
                    collectionDistribution.setState(state);
                    collectionDistributionList.add(collectionDistribution);
                }
                collectionDetail.setCollectionDistributionList(collectionDistributionList);
                collectionDetail.setDetailContent(detailContent);
                collectionDetailQueryResult.setCollectionDetail(collectionDetail);
                collectionDetailQueryResult.setCollectionDetailQueryResultEnum(ServiceResultEnum.SUCCESS);
                return collectionDetailQueryResult;
            }
            throw new ServerErrorException("移动图书馆系统异常");
        } catch (IOException e) {
            log.error("查询馆藏图书详细信息异常：" + e);
            collectionDetailQueryResult.setCollectionDetailQueryResultEnum(ServiceResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("查询馆藏图书详细信息异常：" + e);
            collectionDetailQueryResult.setCollectionDetailQueryResultEnum(ServiceResultEnum.SERVER_ERROR);
        }
        return collectionDetailQueryResult;
    }
}
