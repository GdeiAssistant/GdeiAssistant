package com.linguancheng.gdeiassistant.Service.SchoolNews;

import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.New.NewMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.NewInfo;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Tools.SchoolNewsUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;


/**
 * 该功能已于2018年02月13日停止使用
 */
@Service
@Deprecated
public class SchoolNewsService {

    @Resource(name = "schoolNewsUrlList")
    private List<String> urls;

    @Resource(name = "newMapper")
    private NewMapper newMapper;

    /**
     * 查找新闻通知信息列表
     *
     * @param start
     * @param size
     * @return
     */
    public BaseResult<List<NewInfo>, BoolResultEnum> QueryNewInfoList(int start, int size) {
        BaseResult<List<NewInfo>, BoolResultEnum> result = new BaseResult<>();
        try {
            List<NewInfo> newInfoList = newMapper.selectNewInfoList(start, size);
            result.setResultType(BoolResultEnum.SUCCESS);
            result.setResultData(newInfoList);
        } catch (Exception e) {
            e.printStackTrace();
            result.setResultType(BoolResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询新闻通知详细信息
     *
     * @param id
     * @return
     */
    public BaseResult<NewInfo, DataBaseResultEnum> QueryNewDetailInfo(int id) {
        BaseResult<NewInfo, DataBaseResultEnum> result = new BaseResult<>();
        try {
            NewInfo newInfo = newMapper.selectNewInfo(id);
            if (newInfo != null) {
                result.setResultData(newInfo);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            } else {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 定时收集新闻通知信息
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
//    @Scheduled(cron = "0 7,12,17 * * * ?")
    public void CollectNews() throws IOException, ExecutionException, InterruptedException {
        List<NewInfo> newInfoList = new ArrayList<>();
        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault();
        httpAsyncClient.start();
        //登录进入新闻信息系统
        HttpPost httpPost = new HttpPost("https://security.gdei.edu.cn/cas/login");
        List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
        basicNameValuePairList.add(new BasicNameValuePair("service", "http://web.gdei.edu.cn/gdei/stuwork/stuadmin/index.html"));
        basicNameValuePairList.add(new BasicNameValuePair("username", "lingc"));
        basicNameValuePairList.add(new BasicNameValuePair("password", "Tom2312."));
        httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
        HttpResponse httpResponse = httpAsyncClient.execute(httpPost, null).get();
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            HttpGet httpGet = new HttpGet(document.select("a").first().attr("href"));
            httpResponse = httpAsyncClient.execute(httpGet, null).get();
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("学生工作_学校门户")) {
                //进入学校新闻信息系统成功，异步获取新闻通知数据
                //遍历每个新闻分区
                for (int i = 0; i < urls.size(); i++) {
                    final int type = i;
                    httpResponse = httpAsyncClient.execute(new HttpGet(urls.get(i)), null).get();
                    Document result = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    //设置当前分区的新闻同步器，等待该分区下所有新闻都收集完成
                    CountDownLatch countDownLatch = new CountDownLatch(result.getElementById("paging").select("li").size());
                    //遍历该分区下的新闻信息
                    for (Element li : result.getElementById("paging").select("li")) {
                        Element a = li.select("a").first();
                        //文字类型新闻通知信息
                        if (!a.attr("href").startsWith("javascript:") && a.attr("href").endsWith(".xhtml")) {
                            final String title = a.attr("title");
                            final String publishDate = li.select("span").size() == 0
                                    ? li.textNodes().get(0).text().substring(1, li.textNodes().get(0).text().length())
                                    : li.select("span").first().text().substring(1, li.select("span").first().text().length());
                            //获取新闻详细信息
                            httpAsyncClient.execute(new HttpGet("http://web.gdei.edu.cn/" + a.attr("href")), new FutureCallback<HttpResponse>() {
                                @Override
                                public void completed(HttpResponse result) {
                                    try {
                                        Document document1 = Jsoup.parse(EntityUtils.toString(result.getEntity()));
                                        String content = null;
                                        if (document1.getElementsByClass("inside-content").size() == 0) {
                                            content = document1.getElementsByClass("detail_text").first()
                                                    .getElementsByAttributeValue("style", "padding-bottom:20px;")
                                                    .first().toString().replace("href=\"resource/", "href=\"" + urls.get(type) + "/resource/")
                                                    .replace("src=\"resource/", "src=\"" + urls.get(type) + "/resource/");
                                        } else {
                                            content = document1.getElementsByClass("inside-content").first().toString().replace("href=\"resource/", "href=\"" + urls.get(type) + "/resource/")
                                                    .replace("src=\"resource/", "src=\"" + urls.get(type) + "/resource/");
                                        }
                                        NewInfo newInfo = new NewInfo();
                                        newInfo.setTitle(title);
                                        newInfo.setType(type);
                                        newInfo.setPublishDate(publishDate);
                                        newInfo.setContent(content);
                                        //保存新闻信息
                                        newInfoList.add(newInfo);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        countDownLatch.countDown();
                                    }
                                }

                                @Override
                                public void failed(Exception ex) {
                                    countDownLatch.countDown();
                                }

                                @Override
                                public void cancelled() {
                                    countDownLatch.countDown();
                                }
                            });
                            //PDF文件通知信息
                        } else if (!a.attr("href").startsWith("javascript:") && a.attr("href").endsWith(".pdf")) {
                            try {
                                NewInfo newInfo = new NewInfo();
                                newInfo.setTitle(a.text());
                                newInfo.setPublishDate(li.select("span").size() == 0
                                        ? li.textNodes().get(0).text().substring(1, li.textNodes().get(0).text().length())
                                        : li.select("span").first().text().substring(1, li.select("span").first().text().length()));
                                newInfo.setType(type);
                                newInfo.setContent(SchoolNewsUtils.CreatePdfDownloadTag(a.text(), a.attr("href")));
                                newInfoList.add(newInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    }
                    countDownLatch.await();
                }
                //保存新闻信息到数据库
                for (NewInfo newInfo : newInfoList) {
                    try {
                        if (StringUtils.isBlank(newMapper.selectNewInfoByTitle(newInfo.getTitle()))) {
                            newMapper.insertNewInfo(newInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                httpAsyncClient.close();
            }
        }
    }
}
