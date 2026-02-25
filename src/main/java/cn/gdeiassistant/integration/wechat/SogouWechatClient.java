package cn.gdeiassistant.integration.wechat;

import cn.gdeiassistant.integration.httpclient.HttpClientSession;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜狗微信搜索防腐层：封装对 weixin.sogou.com 的 HTTP 请求，仅返回 Document 或 byte[]，供 core 解析与组装。
 */
@Component
public class SogouWechatClient {

    private static final String SOGOU_WEIXIN_BASE = "https://weixin.sogou.com";
    private static final String USER_AGENT_MOBILE = "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Mobile Safari/537.36";
    private static final String USER_AGENT_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36";
    private static final int TIMEOUT_SEC = 5;

    /**
     * 访问搜狗微信首页，建立 Cookie（无返回值，仅用于后续请求带 Cookie）。
     */
    public void fetchSogouHome(String sessionId) throws IOException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(SOGOU_WEIXIN_BASE);
            httpClient.execute(httpGet);
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 请求搜狗微信搜索页（公众号 ID 为 query），返回列表页 Document。
     */
    public Document fetchWeixinSearchPage(String sessionId, String wechatAccountId) throws IOException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(SOGOU_WEIXIN_BASE + "/weixin?type=1&s_from=input&query=" + wechatAccountId + "&ie=utf8&_sug_=n&_sug_type");
            httpGet.setHeader("Referer", SOGOU_WEIXIN_BASE);
            httpGet.setHeader("User-Agent", USER_AGENT_MOBILE);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new IOException("HTTP " + httpResponse.getStatusLine().getStatusCode());
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 获取反爬验证码图片二进制（用于识别），返回 PNG byte[]。
     */
    public byte[] fetchAntispiderImage(String sessionId, String imageRelativePath, String refererQuery) throws IOException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(SOGOU_WEIXIN_BASE + "/antispider/" + imageRelativePath);
            httpGet.setHeader("Referer", SOGOU_WEIXIN_BASE + "/antispider/?query=" + refererQuery);
            httpGet.setHeader("Sec-Fetch-Mode", "no-cors");
            httpGet.setHeader("Sec-Fetch-Site", "same-origin");
            httpGet.setHeader("User-Agent", USER_AGENT_DESKTOP);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            return EntityUtils.toByteArray(httpResponse.getEntity());
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 提交反爬验证码表单，返回结果页 Document（body 可能为 JSON 文本，由调用方解析）。
     */
    public Document submitAntispiderForm(String sessionId, String checkCode, String fromVal, String typeVal, String refererQuery) throws IOException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            List<BasicNameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("c", checkCode));
            form.add(new BasicNameValuePair("r", fromVal != null ? fromVal : ""));
            form.add(new BasicNameValuePair("v", typeVal != null ? typeVal : ""));
            HttpPost httpPost = new HttpPost(SOGOU_WEIXIN_BASE + "/antispider/thank.php");
            httpPost.setHeader("Referer", SOGOU_WEIXIN_BASE + "/antispider/?query=" + refererQuery);
            httpPost.setHeader("Sec-Fetch-Mode", "no-cors");
            httpPost.setHeader("Sec-Fetch-Site", "same-origin");
            httpPost.setHeader("User-Agent", USER_AGENT_DESKTOP);
            httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 验证码通过后再次请求搜索页（Referer 为 antispider），返回 Document。
     */
    public Document fetchWeixinSearchPageAfterAntispider(String sessionId, String wechatAccountId) throws IOException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(SOGOU_WEIXIN_BASE + "/weixin?type=1&s_from=input&query=" + wechatAccountId + "&ie=utf8&_sug_=n&_sug_type");
            httpGet.setHeader("Referer", SOGOU_WEIXIN_BASE + "/antispider/?query=" + wechatAccountId);
            httpGet.setHeader("Sec-Fetch-Mode", "navigate");
            httpGet.setHeader("Sec-Fetch-Site", "same-origin");
            httpGet.setHeader("Sec-Fetch-User", "?1");
            httpGet.setHeader("User-Agent", USER_AGENT_DESKTOP);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new IOException("HTTP " + httpResponse.getStatusLine().getStatusCode());
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }
}
