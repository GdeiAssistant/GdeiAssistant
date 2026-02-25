package cn.gdeiassistant.core.information.service.SchoolNews;

import cn.gdeiassistant.common.pojo.Config.NewsConfig;
import cn.gdeiassistant.common.pojo.Entity.NewInfo;
import cn.gdeiassistant.common.pojo.Entity.RSSNewInfo;
import cn.gdeiassistant.core.news.repository.NewDao;
import cn.gdeiassistant.common.tools.Utils.SchoolNewsUtils;
import cn.gdeiassistant.common.tools.Utils.XMLParseUtils;
import cn.gdeiassistant.integration.cas.CasClient;
import cn.gdeiassistant.integration.cas.pojo.CasSessionCredential;
import cn.gdeiassistant.integration.news.NewsClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import cn.gdeiassistant.common.condition.NewsAdminAccountCondition;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("production")
@Conditional(NewsAdminAccountCondition.class)
public class SchoolNewsCornService {

    private final Logger logger = LoggerFactory.getLogger(SchoolNewsCornService.class);

    @Autowired
    private NewDao newDao;

    @Resource(name = "newsUrlsList")
    private List<List<String>> newsUrlsList;

    @Resource(name = "rssNewsUrlsList")
    private List<String> rssNewsUrlsList;

    @Autowired
    private NewsConfig newsConfig;

    @Autowired
    private CasClient casClient;

    @Autowired
    private NewsClient newsClient;

    /**
     * 收集新闻通知信息（可由 Scheduler 或 HTTP /cron/news 触发）。
     */
    public void collectNews() throws IOException {
        logger.info("{}启动了收集新闻通知信息的任务", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        List<NewInfo> newInfoList = new ArrayList<>();
        try {
            CasSessionCredential credential = casClient.login(
                    newsConfig.getAdminAccount(),
                    newsConfig.getAdminPassword(),
                    "http://web.gdei.edu.cn/gdei/stuwork/stuadmin/index.html"
            );
            newsClient.enterNewsSystem(credential);

            for (int i = 0; i < newsUrlsList.size(); i++) {
                final int type = i;
                for (int j = 0; j < newsUrlsList.get(i).size(); j++) {
                    String listUrl = newsUrlsList.get(i).get(j);
                    Document result;
                    try {
                        result = newsClient.fetchPage(credential, listUrl);
                    } catch (Exception e) {
                        logger.error("抓取校园新闻列表页失败，url={}", listUrl, e);
                        continue;
                    }
                    for (Element li : result.getElementById("paging").select("li")) {
                        Element a = li.select("a").first();
                        if (!a.attr("href").startsWith("javascript:") && a.attr("href").endsWith(".xhtml")) {
                            String title = a.attr("title");
                            String publishDate = li.select("span").size() == 0
                                    ? li.textNodes().get(0).text().substring(1)
                                    : li.select("span").first().text().substring(1);
                            String id = a.attr("href");
                            int newIndex = j;
                            try {
                                Document document1 = newsClient.fetchPage(credential, "http://web.gdei.edu.cn" + a.attr("href"));
                                String content;
                                if (document1.getElementsByClass("inside-content").size() == 0) {
                                    document1.select("img").attr("width", "100%");
                                    document1.select("img").attr("height", "auto");
                                    Elements p = document1.select("p");
                                    for (Element element : p) {
                                        if (element.attr("style").contains("text-indent") && element.select("img").size() > 0) {
                                            element.attr("style", element.attr("style").replace("text-indent", ""));
                                        }
                                    }
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
                                        } else {
                                            element.attr("src", "http://web.gdei.edu.cn/" + element.attr("src"));
                                        }
                                    }
                                    Elements download = document1.getElementsByClass("detail_text").first().getElementsByAttributeValue("style", "padding-bottom:20px;").first().getElementsByAttributeValue("target", "_blank");
                                    for (Element element : download) {
                                        element.attr("onclick", "downloadFile('" + element.attr("href") + "')");
                                        element.attr("href", "javascript:");
                                    }
                                    content = document1.getElementsByClass("detail_text").first()
                                            .getElementsByAttributeValue("style", "padding-bottom:20px;").first().toString();
                                } else {
                                    Elements hrefs = document1.getElementsByClass("inside-content").first().getElementsByAttribute("href");
                                    Elements sources = document1.getElementsByClass("inside-content").first().getElementsByAttribute("src");
                                    for (Element element : hrefs) {
                                        if (element.attr("href").startsWith("resource")) {
                                            element.attr("href", newsUrlsList.get(type).get(newIndex) + "/" + element.attr("href"));
                                        } else if (element.attr("href").startsWith("mailto:")
                                                || element.attr("href").startsWith("http://")
                                                || element.attr("href").startsWith("https://")) {
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
                                        } else {
                                            element.attr("src", "http://web.gdei.edu.cn/" + element.attr("src"));
                                        }
                                    }
                                    Elements download = document1.getElementsByClass("inside-content").first().getElementsByAttributeValue("target", "_blank");
                                    for (Element element : download) {
                                        element.attr("onclick", "downloadFile('" + element.attr("href") + "')");
                                        element.attr("href", "javascript:");
                                    }
                                    document1.select("img").attr("width", "100%");
                                    document1.select("img").attr("height", "auto");
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
                                newInfoList.add(newInfo);
                            } catch (Exception e) {
                                logger.error("解析校园新闻详情页失败，href={}", a.attr("href"), e);
                            }
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
                                newInfo.setContent(SchoolNewsUtils.createDownloadTag(a.text(), a.attr("href")));
                                newInfoList.add(newInfo);
                            } catch (Exception e) {
                                logger.error("解析校园 PDF 通知失败，href={}", a.attr("href"), e);
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
                                newInfo.setContent(SchoolNewsUtils.createDownloadTag(a.text(), a.attr("href")));
                                newInfoList.add(newInfo);
                            } catch (Exception e) {
                                logger.error("解析校园下载型通知失败，href={}", a.attr("href"), e);
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < rssNewsUrlsList.size(); i++) {
                String rssUrl = rssNewsUrlsList.get(i);
                String result;
                    try {
                        result = newsClient.fetchText(credential, rssUrl);
                    } catch (Exception e) {
                        logger.error("抓取 RSS 新闻源失败，rssUrl={}", rssUrl, e);
                        continue;
                    }
                List<RSSNewInfo> rssNewInfoList = XMLParseUtils.parseRssNewsXml(result);
                if (!rssNewInfoList.isEmpty()) {
                    for (RSSNewInfo rssNewInfo : rssNewInfoList) {
                        try {
                            String link = rssNewInfo.getLink();
                            String idPath;
                            String[] split = link.split("/");
                            StringBuilder stringBuilder = new StringBuilder("/");
                            for (int k = 3; k < split.length; k++) {
                                stringBuilder.append(split[k]);
                                if (k + 1 != split.length) {
                                    stringBuilder.append("/");
                                }
                            }
                            idPath = stringBuilder.toString();
                            String content;
                            if (link.endsWith(".xhtml") || link.endsWith(".html")) {
                                Document page = Jsoup.parse(newsClient.fetchText(credential, link));
                                page.select("img").attr("width", "100%");
                                page.select("img").attr("height", "auto");
                                Elements p = page.select("p");
                                for (Element element : p) {
                                    if (element.attr("style").contains("text-indent") && element.select("img").size() > 0) {
                                        element.attr("style", element.attr("style").replace("text-indent", ""));
                                    }
                                }
                                String[] linkSplit = link.split("/");
                                StringBuilder url = new StringBuilder();
                                for (int k = 0; k < linkSplit.length - 1; k++) {
                                    url.append(linkSplit[k]).append("/");
                                }
                                Elements hrefs = page.getElementsByClass("text").first().getElementsByAttribute("href");
                                Elements sources = page.getElementsByClass("text").first().getElementsByAttribute("src");
                                for (Element element : hrefs) {
                                    if (!element.attr("href").startsWith("http://web.gdei.edu.cn/")) {
                                        if (element.attr("href").startsWith("resource")) {
                                            element.attr("href", url + "/" + element.attr("href"));
                                        } else if (element.attr("href").startsWith("mailto:")
                                                || element.attr("href").startsWith("http://")
                                                || element.attr("href").startsWith("https://")) {
                                        } else {
                                            element.attr("href", "http://web.gdei.edu.cn/" + element.attr("href"));
                                        }
                                    }
                                }
                                for (Element element : sources) {
                                    if (!element.attr("src").startsWith("http://web.gdei.edu.cn/")) {
                                        if (element.attr("src").startsWith("resource")) {
                                            element.attr("src", url + "/" + element.attr("src"));
                                        } else if (element.attr("src").startsWith("mailto:")
                                                || element.attr("src").startsWith("http://")
                                                || element.attr("src").startsWith("https://")) {
                                        } else {
                                            element.attr("src", "http://web.gdei.edu.cn/" + element.attr("src"));
                                        }
                                    }
                                }
                                Elements download = page.getElementsByClass("text").first().getElementsByAttributeValue("target", "_blank");
                                for (Element element : download) {
                                    element.attr("onclick", "downloadFile('" + element.attr("href") + "')");
                                    element.attr("href", "javascript:");
                                }
                                content = page.getElementsByClass("text").first().toString();
                            } else {
                                content = SchoolNewsUtils.createDownloadTag(rssNewInfo.getTitle(), rssNewInfo.getLink());
                            }
                            rssNewInfo.setId(DigestUtils.sha1Hex(idPath));
                            rssNewInfo.setContent(content);
                            rssNewInfo.setType(4);
                            newInfoList.add(rssNewInfo);
                        } catch (Exception e) {
                            logger.error("解析 RSS 新闻详情失败，link={}", rssNewInfo.getLink(), e);
                        }
                    }
                }
            }

            newDao.saveNewInfoList(newInfoList);
        } catch (Exception e) {
            logger.error("收集校园新闻任务执行异常", e);
        }
    }
}
