package edu.gdei.gdeiassistant.Service.SchoolNews;

import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.NewInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.RSSNewInfo;
import edu.gdei.gdeiassistant.Repository.Mongodb.New.NewDao;
import edu.gdei.gdeiassistant.Tools.SchoolNewsUtils;
import edu.gdei.gdeiassistant.Tools.XMLParseUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Service
public class SchoolNewsService {

    private Logger logger = LoggerFactory.getLogger(SchoolNewsService.class);

    @Resource(name = "newsUrlsList")
    private List<List<String>> newsUrlsList;

    @Resource(name = "rssNewsUrlsList")
    private List<String> rssNewsUrlsList;

    @Autowired
    private NewDao newDao;

    private String adminAccount;

    private String adminPassword;

    @Value("#{propertiesReader['news.admin.account']}")
    public void setAdminAccount(String adminAccount) {
        this.adminAccount = adminAccount;
    }

    @Value("#{propertiesReader['news.admin.password']}")
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * 查找新闻通知信息列表
     *
     * @param type
     * @param start
     * @param size
     * @return
     */
    public List<NewInfo> QueryNewInfoList(int type, int start, int size) throws DataNotExistException {
        List<NewInfo> newInfoList = newDao.queryNewInfoList(type, start, size);
        if (newInfoList != null && !newInfoList.isEmpty()) {
            return newInfoList;
        }
        throw new DataNotExistException("没有更多的新闻通知信息");
    }

    /**
     * 查询新闻通知详细信息
     *
     * @param id
     * @return
     */
    public NewInfo QueryNewDetailInfo(String id) throws DataNotExistException {
        NewInfo newInfo = newDao.queryNewInfo(id);
        if (newInfo != null) {
            return newInfo;
        }
        throw new DataNotExistException("没有对应的新闻通知信息");
    }

    /**
     * 定时收集新闻通知信息
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Scheduled(cron = "0 0 7,12,17 * * ?")
    public void CollectNews() throws IOException, ExecutionException, InterruptedException {
        logger.info("{}启动了收集新闻通知信息的任务", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        List<NewInfo> newInfoList = new ArrayList<>();
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setSelectInterval(3000).setSoTimeout(3000).setConnectTimeout(3000).build();
        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom().setDefaultIOReactorConfig(ioReactorConfig).build();
        httpAsyncClient.start();
        //登录进入新闻信息系统
        HttpGet httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login");
        HttpResponse httpResponse = httpAsyncClient.execute(httpGet, null).get();
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        HttpPost httpPost = new HttpPost("https://security.gdei.edu.cn/cas/login");
        List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
        basicNameValuePairList.add(new BasicNameValuePair("service", "http://web.gdei.edu.cn/gdei/stuwork/stuadmin/index.html"));
        basicNameValuePairList.add(new BasicNameValuePair("username", adminAccount));
        basicNameValuePairList.add(new BasicNameValuePair("password", adminPassword));
        basicNameValuePairList.add(new BasicNameValuePair("imageField.x", "0"));
        basicNameValuePairList.add(new BasicNameValuePair("imageField.y", "0"));
        basicNameValuePairList.add(new BasicNameValuePair("tokens", document.getElementById("tokens").val()));
        basicNameValuePairList.add(new BasicNameValuePair("stamp", document.getElementById("stamp").val()));
        httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
        httpResponse = httpAsyncClient.execute(httpPost, null).get();
        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            httpGet = new HttpGet(document.select("a").first().attr("href"));
            httpResponse = httpAsyncClient.execute(httpGet, null).get();
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //进入学校新闻信息系统成功，异步获取新闻通知数据
                //遍历每个新闻分区
                for (int i = 0; i < newsUrlsList.size(); i++) {
                    final int type = i;
                    for (int j = 0; j < newsUrlsList.get(i).size(); j++) {
                        httpResponse = httpAsyncClient.execute(new HttpGet(newsUrlsList.get(i).get(j)), null).get();
                        if (httpResponse.getStatusLine().getStatusCode() != 200) {
                            continue;
                        }
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
                                        ? li.textNodes().get(0).text().substring(1)
                                        : li.select("span").first().text().substring(1);
                                final String id = a.attr("href");
                                final int newIndex = j;
                                //获取新闻详细信息
                                httpAsyncClient.execute(new HttpGet("http://web.gdei.edu.cn" + a.attr("href")), new FutureCallback<HttpResponse>() {
                                    @Override
                                    public void completed(HttpResponse result) {
                                        try {
                                            Document document1 = Jsoup.parse(EntityUtils.toString(result.getEntity()));
                                            String content = null;
                                            if (document1.getElementsByClass("inside-content").size() == 0) {
                                                //调整图片尺寸
                                                document1.select("img").attr("width", "100%");
                                                document1.select("img").attr("height", "auto");
                                                //调整网页格式
                                                Elements p = document1.select("p");
                                                for (Element element : p) {
                                                    if (element.attr("style").contains("text-indent") && element.select("img").size() > 0) {
                                                        element.attr("style", element.attr("style").replace("text-indent", ""));
                                                    }
                                                }
                                                //补全资源引用地址
                                                Elements hrefs = document1.getElementsByClass("detail_text").first()
                                                        .getElementsByAttributeValue("style", "padding-bottom:20px;").first().getElementsByAttribute("href");
                                                Elements sources = document1.getElementsByClass("detail_text").first()
                                                        .getElementsByAttributeValue("style", "padding-bottom:20px;").first().getElementsByAttribute("src");
                                                for (Element element : hrefs) {
                                                    if (element.attr("href").startsWith("resource")) {
                                                        element.attr("href", newsUrlsList.get(type).get(newIndex) + "/" + element.attr("href"));
                                                    } else if (element.attr("href").startsWith("mailto:")
                                                            || element.attr("href").startsWith("http://")
                                                            || element.attr("href").startsWith("https://")) {
                                                        //发送邮件前缀、完整的URL路径，不做补全转换
                                                    } else {
                                                        element.attr("href", "http://web.gdei.edu.cn/" + element.attr("href"));
                                                    }
                                                }
                                                for (Element element : sources) {
                                                    if (element.attr("src").startsWith("resource")) {
                                                        element.attr("src", newsUrlsList.get(type).get(newIndex) + "/" + element.attr("src"));
                                                    } else if (element.attr("src").startsWith("mailto:")
                                                            || element.attr("src").startsWith("http://")
                                                            || element.attr("src").startsWith("https://")) {
                                                        //发送邮件前缀、完整的URL路径，不做补全转换
                                                    } else {
                                                        element.attr("src", "http://web.gdei.edu.cn/" + element.attr("src"));
                                                    }
                                                }
                                                //替换下载链接
                                                Elements download = document1.getElementsByClass("detail_text").first().getElementsByAttributeValue("style", "padding-bottom:20px;").first().getElementsByAttributeValue("target", "_blank");
                                                for (Element element : download) {
                                                    element.attr("onclick", "downloadFile('" + element.attr("href") + "')");
                                                    element.attr("href", "javascript:");
                                                }
                                                content = document1.getElementsByClass("detail_text").first()
                                                        .getElementsByAttributeValue("style", "padding-bottom:20px;").first().toString();
                                            } else {
                                                //补全资源引用地址
                                                Elements hrefs = document1.getElementsByClass("inside-content").first().getElementsByAttribute("href");
                                                Elements sources = document1.getElementsByClass("inside-content").first().getElementsByAttribute("src");
                                                for (Element element : hrefs) {
                                                    if (element.attr("href").startsWith("resource")) {
                                                        element.attr("href", newsUrlsList.get(type).get(newIndex) + "/" + element.attr("href"));
                                                    } else if (element.attr("href").startsWith("mailto:")
                                                            || element.attr("href").startsWith("http://")
                                                            || element.attr("href").startsWith("https://")) {
                                                        //发送邮件前缀、完整的URL路径，不做补全转换
                                                    } else {
                                                        element.attr("href", "http://web.gdei.edu.cn/" + element.attr("href"));
                                                    }
                                                }
                                                for (Element element : sources) {
                                                    if (element.attr("src").startsWith("resource")) {
                                                        element.attr("src", newsUrlsList.get(type).get(newIndex) + "/" + element.attr("src"));
                                                    } else if (element.attr("src").startsWith("mailto:")
                                                            || element.attr("src").startsWith("http://")
                                                            || element.attr("src").startsWith("https://")) {
                                                        //发送邮件前缀、完整的URL路径，不做补全转换
                                                    } else {
                                                        element.attr("src", "http://web.gdei.edu.cn/" + element.attr("src"));
                                                    }
                                                }
                                                //替换下载链接
                                                Elements download = document1.getElementsByClass("inside-content").first().getElementsByAttributeValue("target", "_blank");
                                                for (Element element : download) {
                                                    element.attr("onclick", "downloadFile('" + element.attr("href") + "')");
                                                    element.attr("href", "javascript:");
                                                }
                                                //调整图片尺寸
                                                document1.select("img").attr("width", "100%");
                                                document1.select("img").attr("height", "auto");
                                                //调整网页格式
                                                Elements p = document1.select("p");
                                                for (Element element : p) {
                                                    if (element.attr("style").contains("text-indent") && element.select("img").size() > 0) {
                                                        element.attr("style", element.attr("style").replace("text-indent", ""));
                                                    }
                                                }
                                                content = document1.getElementsByClass("inside-content").first().toString();
                                            }
                                            NewInfo newInfo = new NewInfo();
                                            newInfo.setId(DigestUtils.sha1Hex(id));
                                            newInfo.setTitle(title);
                                            newInfo.setType(type);
                                            newInfo.setPublishDate(new SimpleDateFormat("yyyy-MM-dd").parse(publishDate));
                                            newInfo.setContent(content);
                                            //保存新闻信息
                                            newInfoList.add(newInfo);
                                        } catch (
                                                Exception e) {
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
                                    String id = a.attr("href");
                                    String title = a.text();
                                    String publishDate = li.select("span").size() == 0
                                            ? li.textNodes().get(0).text().substring(1)
                                            : li.select("span").first().text().substring(1);
                                    NewInfo newInfo = new NewInfo();
                                    newInfo.setId(DigestUtils.sha1Hex(id));
                                    newInfo.setTitle(title);
                                    newInfo.setPublishDate(new SimpleDateFormat("yyyy-MM-dd").parse(publishDate));
                                    newInfo.setType(type);
                                    newInfo.setContent(SchoolNewsUtils.CreateDownloadTag(a.text(), a.attr("href")));
                                    newInfoList.add(newInfo);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    countDownLatch.countDown();
                                }
                            } else {
                                try {
                                    String id = a.attr("href");
                                    String title = a.text();
                                    String publishDate = li.select("span").size() == 0
                                            ? li.textNodes().get(0).text().substring(1)
                                            : li.select("span").first().text().substring(1);
                                    NewInfo newInfo = new NewInfo();
                                    newInfo.setId(DigestUtils.sha1Hex(id));
                                    newInfo.setTitle(title);
                                    newInfo.setPublishDate(new SimpleDateFormat("yyyy-MM-dd").parse(publishDate));
                                    newInfo.setType(type);
                                    newInfo.setContent(SchoolNewsUtils.CreateDownloadTag(a.text(), a.attr("href")));
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
                }
                //加载RSS订阅新闻
                for (int i = 0; i < rssNewsUrlsList.size(); i++) {
                    httpResponse = httpAsyncClient.execute(new HttpGet(rssNewsUrlsList.get(i)), null).get();
                    if (httpResponse.getStatusLine().getStatusCode() != 200) {
                        continue;
                    }
                    //解析XML数据为新闻信息列表
                    String result = EntityUtils.toString(httpResponse.getEntity());
                    List<RSSNewInfo> rssNewInfoList = XMLParseUtils.ParseRSSNewsXML(result);
                    if (!rssNewInfoList.isEmpty()) {
                        //设置当前分区的新闻同步器，等待分区下的所有新闻都加载完毕后再读取下一分区
                        CountDownLatch countDownLatch = new CountDownLatch(rssNewInfoList.size());
                        for (RSSNewInfo rssNewInfo : rssNewInfoList) {
                            httpAsyncClient.execute(new HttpGet(rssNewInfo.getLink()), new FutureCallback<HttpResponse>() {
                                @Override
                                public void completed(HttpResponse result) {
                                    try {
                                        //生成新闻ID和内容
                                        String[] split = rssNewInfo.getLink().split("/");
                                        StringBuilder stringBuilder = new StringBuilder("/");
                                        for (int i = 3; i < split.length; i++) {
                                            stringBuilder.append(split[i]);
                                            if (i + 1 != split.length) {
                                                stringBuilder.append("/");
                                            }
                                        }
                                        String id = stringBuilder.toString();
                                        String content = null;
                                        if (rssNewInfo.getLink().endsWith(".xhtml") || rssNewInfo.getLink().endsWith(".html")) {
                                            Document page = Jsoup.parse(EntityUtils.toString(result.getEntity()));
                                            //调整图片尺寸
                                            page.select("img").attr("width", "100%");
                                            page.select("img").attr("height", "auto");
                                            //调整网页格式
                                            Elements p = page.select("p");
                                            for (Element element : p) {
                                                if (element.attr("style").contains("text-indent") && element.select("img").size() > 0) {
                                                    element.attr("style", element.attr("style").replace("text-indent", ""));
                                                }
                                            }
                                            //获取图片URL前缀
                                            String[] linkSplit = rssNewInfo.getLink().split("/");
                                            StringBuilder url = new StringBuilder();
                                            for (int i = 0; i < linkSplit.length - 1; i++) {
                                                url.append(linkSplit[i]).append("/");
                                            }
                                            //补全资源引用地址
                                            Elements hrefs = page.getElementsByClass("text").first().getElementsByAttribute("href");
                                            Elements sources = page.getElementsByClass("text").first().getElementsByAttribute("src");
                                            for (Element element : hrefs) {
                                                if (!element.attr("href").startsWith("http://web.gdei.edu.cn/")) {
                                                    if (element.attr("href").startsWith("resource")) {
                                                        element.attr("href", url.toString() + "/" + element.attr("href"));
                                                    } else if (element.attr("href").startsWith("mailto:")
                                                            || element.attr("href").startsWith("http://")
                                                            || element.attr("href").startsWith("https://")) {
                                                        //发送邮件前缀、完整的URL路径，不做补全转换
                                                    } else {
                                                        element.attr("href", "http://web.gdei.edu.cn/" + element.attr("href"));
                                                    }
                                                }
                                            }
                                            for (Element element : sources) {
                                                if (!element.attr("src").startsWith("http://web.gdei.edu.cn/")) {
                                                    if (element.attr("src").startsWith("resource")) {
                                                        element.attr("src", url.toString() + "/" + element.attr("src"));
                                                    } else if (element.attr("src").startsWith("mailto:")
                                                            || element.attr("src").startsWith("http://")
                                                            || element.attr("src").startsWith("https://")) {
                                                        //发送邮件前缀、完整的URL路径，不做补全转换
                                                    } else {
                                                        element.attr("src", "http://web.gdei.edu.cn/" + element.attr("src"));
                                                    }
                                                }
                                            }
                                            //替换下载链接
                                            Elements download = page.getElementsByClass("text").first().getElementsByAttributeValue("target", "_blank");
                                            for (Element element : download) {
                                                element.attr("onclick", "downloadFile('" + element.attr("href") + "')");
                                                element.attr("href", "javascript:");
                                            }
                                            content = page.getElementsByClass("text").first().toString();
                                        } else {
                                            content = SchoolNewsUtils.CreateDownloadTag(rssNewInfo.getTitle(), rssNewInfo.getLink());
                                        }
                                        rssNewInfo.setId(DigestUtils.sha1Hex(id));
                                        rssNewInfo.setContent(content);
                                        //新闻分类属于综合信息
                                        rssNewInfo.setType(4);
                                        newInfoList.add(rssNewInfo);
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
                        }
                        countDownLatch.await();
                    }
                }
                //保存新闻信息到数据库
                newDao.saveNewInfoList(newInfoList);
                httpAsyncClient.close();
            }
        }
    }
}
