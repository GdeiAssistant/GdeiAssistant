package cn.gdeiassistant.integration.news;

import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import cn.gdeiassistant.common.i18n.Messages;

/**
 * 校园新闻抓取防腐层：直接访问学校公开站点，仅返回 Document 或文本内容。
 */
@Component
public class NewsClient {

    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; GdeiAssistantNewsBot/2.0)";

    private Connection.Response executeGet(String url) throws IOException, ServerErrorException {
        Connection.Response response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute();
        if (response.statusCode() != 200) {
            throw new ServerErrorException(Messages.get("news.siteAccess.failed"));
        }
        return response;
    }

    /**
     * 访问指定 URL，返回 Jsoup Document。
     */
    public Document fetchPage(String url)
            throws IOException, ServerErrorException {
        return executeGet(url).parse();
    }

    /**
     * 访问指定 URL，返回原始响应文本。
     */
    public String fetchText(String url)
            throws IOException, ServerErrorException {
        return executeGet(url).body();
    }
}
