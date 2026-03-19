package cn.gdeiassistant.core.information.service.SchoolNews;

import cn.gdeiassistant.common.constant.ResourcesConstantUtils;
import cn.gdeiassistant.common.pojo.Entity.NewInfo;
import cn.gdeiassistant.core.news.repository.NewDao;
import cn.gdeiassistant.integration.news.NewsClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Profile("production")
public class SchoolNewsCornService {

    private static final int MAX_PAGES_PER_CATEGORY = 12;
    private static final int MAX_ITEMS_PER_CATEGORY = 216;
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Logger logger = LoggerFactory.getLogger(SchoolNewsCornService.class);

    @Autowired
    private NewDao newDao;

    @Autowired
    private NewsClient newsClient;

    /**
     * 收集新闻通知信息（可由 Scheduler 或 HTTP /cron/news 触发）。
     */
    public void collectNews() {
        logger.info("{}启动了收集新闻通知信息的任务", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        Map<String, NewInfo> newInfoMap = new LinkedHashMap<>();
        try {
            for (int i = 0; i < ResourcesConstantUtils.SCHOOL_NEWS_CATEGORY_URL_LIST.length; i++) {
                int type = i + 1;
                collectCategoryNews(type, ResourcesConstantUtils.SCHOOL_NEWS_CATEGORY_URL_LIST[i], newInfoMap);
            }
            if (!newInfoMap.isEmpty()) {
                newDao.saveNewInfoList(new ArrayList<>(newInfoMap.values()));
            }
            logger.info("校园新闻抓取完成，本次同步 {} 条记录", newInfoMap.size());
        } catch (Exception e) {
            logger.error("收集校园新闻任务执行异常", e);
        }
    }

    private void collectCategoryNews(int type, String listUrl, Map<String, NewInfo> resultMap) {
        String currentPageUrl = listUrl;
        int pageCount = 0;
        int categorySavedCount = 0;
        int consecutiveKnownPages = 0;

        while (currentPageUrl != null
                && pageCount < MAX_PAGES_PER_CATEGORY
                && categorySavedCount < MAX_ITEMS_PER_CATEGORY) {
            pageCount++;
            Document listPage;
            try {
                listPage = newsClient.fetchPage(currentPageUrl);
            } catch (Exception e) {
                logger.error("抓取校园新闻列表页失败，type={}, url={}", type, currentPageUrl, e);
                break;
            }

            Elements items = listPage.select("ul.news_list li.news");
            if (items.isEmpty()) {
                items = listPage.select("ul.news_list li");
            }
            if (items.isEmpty()) {
                logger.warn("校园新闻列表页结构未匹配到新闻条目，type={}, url={}", type, currentPageUrl);
                break;
            }

            int pageChangedCount = 0;
            for (Element item : items) {
                Element linkElement = item.selectFirst("a[href]");
                if (linkElement == null) {
                    continue;
                }
                String detailUrl = normalizeAbsoluteUrl(linkElement.absUrl("href"));
                if (detailUrl.isEmpty()) {
                    continue;
                }
                String id = DigestUtils.sha1Hex(detailUrl);
                NewInfo existing = resultMap.get(id);
                if (existing == null) {
                    existing = newDao.queryNewInfo(id);
                }
                if (resultMap.containsKey(id) && existing != null) {
                    continue;
                }

                try {
                    NewInfo newInfo = buildNewsItem(type, item, linkElement, detailUrl, id, existing);
                    if (newInfo != null && shouldSaveNews(existing, newInfo)) {
                        resultMap.put(id, newInfo);
                        pageChangedCount++;
                        categorySavedCount++;
                    }
                } catch (Exception e) {
                    logger.error("解析校园新闻详情失败，type={}, href={}", type, detailUrl, e);
                }
            }

            consecutiveKnownPages = pageChangedCount == 0 ? consecutiveKnownPages + 1 : 0;
            if (consecutiveKnownPages >= 2) {
                break;
            }
            currentPageUrl = extractNextPageUrl(listPage);
        }
    }

    private NewInfo buildNewsItem(int type, Element listItem, Element linkElement, String detailUrl, String id,
            NewInfo existing)
            throws Exception {
        String fallbackTitle = firstNonBlank(linkElement.attr("title"), linkElement.text(), "新闻通知");
        String content = "暂无详细内容";
        String title = fallbackTitle;
        Document detailPage = null;

        if (isArticlePage(detailUrl)) {
            detailPage = newsClient.fetchPage(detailUrl);
            title = firstNonBlank(
                    extractText(detailPage.selectFirst(".arti_title")),
                    detailPage.title(),
                    fallbackTitle
            );
            content = extractArticleContent(detailPage);
        } else {
            content = buildAttachmentContent(detailUrl, fallbackTitle);
        }
        Date publishDate = resolvePublishDate(extractPublishDateText(listItem), detailPage, existing);

        NewInfo newInfo = new NewInfo();
        newInfo.setId(id);
        newInfo.setType(type);
        newInfo.setTitle(title);
        newInfo.setPublishDate(publishDate);
        newInfo.setContent(firstNonBlank(content, "暂无详细内容"));
        newInfo.setSourceUrl(detailUrl);
        return newInfo;
    }

    private boolean shouldSaveNews(NewInfo existing, NewInfo next) {
        if (existing == null) {
            return true;
        }
        return !sameText(existing.getTitle(), next.getTitle())
                || !sameDate(existing.getPublishDate(), next.getPublishDate())
                || !sameText(existing.getContent(), next.getContent())
                || !sameText(existing.getSourceUrl(), next.getSourceUrl());
    }

    private boolean sameText(String left, String right) {
        return firstNonBlank(left, "").equals(firstNonBlank(right, ""));
    }

    private boolean sameDate(Date left, Date right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }

    private String extractPublishDateText(Element listItem) {
        String timeText = extractText(listItem.selectFirst(".news_time"));
        if (!timeText.isEmpty()) {
            return timeText;
        }
        return listItem.text();
    }

    private String extractDetailPublishDateText(Document detailPage) {
        return firstNonBlank(
                extractText(detailPage.selectFirst(".arti_metas .arti_update")),
                extractText(detailPage.selectFirst(".arti_update")),
                extractText(detailPage.selectFirst(".arti_metas")),
                extractText(detailPage.selectFirst(".article-info")),
                extractText(detailPage.selectFirst(".article_info")),
                extractText(detailPage.selectFirst(".meta"))
        );
    }

    private Date resolvePublishDate(String listDateText, Document detailPage, NewInfo existing) {
        LocalDate publishDate = parsePublishDate(listDateText);
        if (publishDate == null && detailPage != null) {
            publishDate = parsePublishDate(extractDetailPublishDateText(detailPage));
        }
        if (publishDate != null) {
            return toDate(publishDate);
        }
        return existing == null ? null : existing.getPublishDate();
    }

    private LocalDate parsePublishDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return LocalDate.parse(matcher.group(1), DATE_FORMATTER);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String extractArticleContent(Document detailPage) {
        Element contentElement = firstNonNull(
                detailPage.selectFirst(".wp_articlecontent"),
                detailPage.selectFirst(".article"),
                detailPage.selectFirst(".detail_text"),
                detailPage.selectFirst(".inside-content"),
                detailPage.selectFirst(".text")
        );
        if (contentElement == null) {
            return "暂无详细内容";
        }

        Element cloned = contentElement.clone();
        cloned.select("script,style,noscript,iframe").remove();

        String attachments = collectAttachmentLinks(cloned);
        String html = cloned.html()
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)</li>", "\n")
                .replaceAll("(?i)</tr>", "\n")
                .replaceAll("(?i)</h[1-6]>", "\n");
        String text = Parser.unescapeEntities(html.replaceAll("<[^>]+>", " "), false);
        text = normalizeContent(text);

        if (!attachments.isEmpty()) {
            return text.isEmpty() ? attachments : text + "\n\n" + attachments;
        }
        return text;
    }

    private String collectAttachmentLinks(Element contentElement) {
        List<String> attachments = new ArrayList<>();
        for (Element anchor : contentElement.select("a[href]")) {
            String href = normalizeAbsoluteUrl(anchor.absUrl("href"));
            if (href.isEmpty() || isArticlePage(href)) {
                continue;
            }
            String title = firstNonBlank(anchor.text(), anchor.attr("title"), href);
            attachments.add(title + "：" + href);
        }
        if (attachments.isEmpty()) {
            return "";
        }
        return "附件链接：\n" + String.join("\n", attachments);
    }

    private String buildAttachmentContent(String detailUrl, String title) {
        return "附件链接：\n" + firstNonBlank(title, "下载附件") + "：" + detailUrl;
    }

    private String extractNextPageUrl(Document listPage) {
        Element nextLink = listPage.selectFirst(".wp_paging .next[href]");
        if (nextLink == null) {
            return null;
        }
        String url = normalizeAbsoluteUrl(nextLink.absUrl("href"));
        return url.isEmpty() ? null : url;
    }

    private boolean isArticlePage(String url) {
        String lowerUrl = url.toLowerCase(Locale.ROOT);
        return lowerUrl.endsWith(".htm")
                || lowerUrl.endsWith(".html")
                || lowerUrl.endsWith(".xhtml");
    }

    private String normalizeAbsoluteUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        String normalized = url.trim();
        if (normalized.startsWith("//")) {
            return "https:" + normalized;
        }
        if (normalized.startsWith("/")) {
            return ResourcesConstantUtils.SCHOOL_NEWS_BASE_URL + normalized;
        }
        return normalized;
    }

    private String normalizeContent(String content) {
        return content
                .replace('\u00A0', ' ')
                .replace("\r", "")
                .replaceAll("[ \\t\\x0B\\f]+", " ")
                .replaceAll(" *\n *", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private String extractText(Element element) {
        return element == null ? "" : element.text().trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private Element firstNonNull(Element... elements) {
        for (Element element : elements) {
            if (element != null) {
                return element;
            }
        }
        return null;
    }
}
