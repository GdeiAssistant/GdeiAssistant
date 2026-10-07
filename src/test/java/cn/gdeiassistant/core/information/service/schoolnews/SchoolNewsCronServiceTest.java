package cn.gdeiassistant.core.information.service.schoolnews;

import cn.gdeiassistant.common.constant.ResourcesConstantUtils;
import cn.gdeiassistant.common.pojo.entity.NewsItem;
import cn.gdeiassistant.core.news.repository.NewsRepository;
import cn.gdeiassistant.integration.news.NewsClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchoolNewsCronServiceTest {
    private final NewsClient client = mock(NewsClient.class);
    private final NewsRepository repository = mock(NewsRepository.class);
    private final SchoolNewsCronService service = new SchoolNewsCronService();
    private static final String BASE = "https://synthetic.invalid/";
    @BeforeEach void setup() throws Exception {
        ReflectionTestUtils.setField(service, "newsClient", client);
        ReflectionTestUtils.setField(service, "newsRepository", repository);
        when(client.fetchPage(anyString())).thenReturn(doc("<div>no news</div>"));
    }
    private static Document doc(String html) { return Jsoup.parse(html, BASE); }
    private static String item(String url, String title, String date) {
        return "<li class='news'><a href='" + url + "' title='" + title + "'>" + title
                + "</a><span class='news_time'>" + date + "</span></li>";
    }
    private List<NewsItem> saved() {
        ArgumentCaptor<List<NewsItem>> rows = ArgumentCaptor.forClass(List.class);
        verify(repository).saveNewsItems(rows.capture()); return rows.getValue();
    }
    @Test void articlesAttachmentsAndDatesAreParsedAndActiveContentRemoved() throws Exception {
        when(client.fetchPage(ResourcesConstantUtils.SCHOOL_NEWS_CATEGORY_URL_LIST[0])).thenReturn(doc(
                "<ul class='news_list'>" + item("article.htm", "list title", "")
                + item("file.pdf", "attachment", "2026-10-07") + "</ul>"));
        when(client.fetchPage(BASE + "article.htm")).thenReturn(doc("""
          <h1 class='arti_title'>合成新闻</h1><span class='arti_update'>2026-10-06</span>
          <div class='wp_articlecontent'><p>第一段</p><p>第二段&nbsp;文字</p>
          <script>secret-script</script><iframe>secret-frame</iframe><style>secret-style</style>
          <a href='notes.pdf'>附件</a><a href='other.htm'>相关文章</a></div>
          """));
        service.collectNews(); var rows = saved(); assertEquals(2, rows.size());
        var article = rows.get(0); assertEquals("合成新闻", article.getTitle()); assertEquals(1, article.getType());
        assertEquals(DigestUtils.sha1Hex(BASE + "article.htm"), article.getId());
        assertEquals(BASE + "article.htm", article.getSourceUrl());
        assertEquals(date("2026-10-06"), article.getPublishDate());
        assertTrue(article.getContent().contains("第一段"));
        assertTrue(article.getContent().contains("第二段 文字"));
        assertTrue(article.getContent().indexOf("第一段") < article.getContent().indexOf("第二段"));
        assertTrue(article.getContent().contains("附件：" + BASE + "notes.pdf"));
        assertFalse(article.getContent().contains("secret-"));
        assertEquals(date("2026-10-07"), rows.get(1).getPublishDate());
        assertTrue(rows.get(1).getContent().contains(BASE + "file.pdf"));
        verify(client, never()).fetchPage(BASE + "file.pdf");
    }
    @Test void duplicateLinksAcrossCategoriesSaveOnceAndPaginationIsFollowed() throws Exception {
        var first = doc("<ul class='news_list'>" + item("same.pdf", "same", "invalid")
                + "<li>missing link</li><li><a href=''>empty</a></li></ul><div class='wp_paging'><a class='next' href='next.htm'>next</a></div>");
        when(client.fetchPage(ResourcesConstantUtils.SCHOOL_NEWS_CATEGORY_URL_LIST[0])).thenReturn(first);
        when(client.fetchPage(ResourcesConstantUtils.SCHOOL_NEWS_CATEGORY_URL_LIST[1])).thenReturn(first);
        when(client.fetchPage(BASE + "next.htm")).thenReturn(doc("<ul class='news_list'>" + item("other.pdf", "other", "2026-99-99") + "</ul>"));
        service.collectNews(); var rows = saved();
        assertEquals(2, rows.size()); assertNull(rows.get(0).getPublishDate()); assertNull(rows.get(1).getPublishDate());
    }
    @Test void unchangedKnownPagesStopAfterTwoAndPreserveHistoricalDate() throws Exception {
        String url = BASE + "known.pdf"; var existing = new NewsItem();
        existing.setTitle("known"); existing.setSourceUrl(url); existing.setPublishDate(date("2024-01-01"));
        existing.setContent("附件链接：\nknown：" + url);
        when(repository.queryNewsItem(DigestUtils.sha1Hex(url))).thenReturn(existing);
        var page = doc("<ul class='news_list'>" + item(url, "known", "no date")
                + "</ul><div class='wp_paging'><a class='next' href='loop.htm'>next</a></div>");
        when(client.fetchPage(ResourcesConstantUtils.SCHOOL_NEWS_CATEGORY_URL_LIST[0])).thenReturn(page);
        when(client.fetchPage(BASE + "loop.htm")).thenReturn(page);
        service.collectNews(); verify(repository, never()).saveNewsItems(anyList());
        verify(client, times(1)).fetchPage(BASE + "loop.htm");
    }
    @Test void aFailedDetailDoesNotLoseOtherArticlesAndListFailuresDoNotPersistEmptyBatch() throws Exception {
        when(client.fetchPage(ResourcesConstantUtils.SCHOOL_NEWS_CATEGORY_URL_LIST[0])).thenReturn(doc("<ul class='news_list'>"
                + item("broken.htm", "broken", "") + item("good.html", "fallback title", "") + "</ul>"));
        when(client.fetchPage(BASE + "broken.htm")).thenThrow(new IOException("synthetic outage"));
        when(client.fetchPage(BASE + "good.html")).thenReturn(doc("<title>document title</title><div class='article'>body</div>"));
        when(client.fetchPage(ResourcesConstantUtils.SCHOOL_NEWS_CATEGORY_URL_LIST[1])).thenThrow(new IOException("synthetic outage"));
        service.collectNews(); var rows = saved(); assertEquals(1, rows.size());
        assertEquals("document title", rows.get(0).getTitle()); assertEquals("body", rows.get(0).getContent());
    }
    private static Date date(String value) { return Date.from(LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant()); }
}
