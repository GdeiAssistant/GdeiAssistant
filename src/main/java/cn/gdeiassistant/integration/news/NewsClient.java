package cn.gdeiassistant.integration.news;

import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.integration.cas.pojo.CasSessionCredential;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 校园新闻抓取防腐层：基于 CAS 会话凭证访问 web.gdei.edu.cn，仅返回 Document 或文本内容。
 */
@Component
public class NewsClient {

    private static final int TIMEOUT_MS = 3000;

    private CloseableHttpClient buildClient(CasSessionCredential credential) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MS)
                .setSocketTimeout(TIMEOUT_MS)
                .build();
        return HttpClients.custom()
                .setDefaultCookieStore(credential.getCookieStore())
                .setDefaultRequestConfig(config)
                .build();
    }

    /**
     * 使用 CAS 会话凭证访问 serviceUrl，进入新闻系统，返回首页 Document。
     */
    public Document enterNewsSystem(CasSessionCredential credential)
            throws IOException, ServerErrorException {
        try (CloseableHttpClient client = buildClient(credential)) {
            HttpGet httpGet = new HttpGet(credential.getServiceUrl());
            HttpResponse httpResponse = client.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("进入新闻系统失败");
            }
            String html = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            return Jsoup.parse(html);
        }
    }

    /**
     * 使用 CAS 会话凭证访问指定 URL，返回 Jsoup Document。
     */
    public Document fetchPage(CasSessionCredential credential, String url)
            throws IOException, ServerErrorException {
        try (CloseableHttpClient client = buildClient(credential)) {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = client.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("新闻系统异常");
            }
            String html = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            return Jsoup.parse(html);
        }
    }

    /**
     * 使用 CAS 会话凭证访问指定 URL，返回原始响应文本（适用于 RSS XML 等）。
     */
    public String fetchText(CasSessionCredential credential, String url)
            throws IOException, ServerErrorException {
        try (CloseableHttpClient client = buildClient(credential)) {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = client.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("新闻系统异常");
            }
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        }
    }
}

