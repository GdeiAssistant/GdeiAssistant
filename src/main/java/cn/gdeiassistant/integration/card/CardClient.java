package cn.gdeiassistant.integration.card;

import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 一卡通（ecard.gdei.edu.cn）网络防腐层。
 * 仅处理支付管理平台侧请求，不包含 security.gdei.edu.cn（CAS）登录；调用方需先完成 CAS+ecard 登录并同步 Cookie 后再调用本 Client。
 */
@Component
public class CardClient {

    private static final String ECARD_BASE = "http://ecard.gdei.edu.cn";
    private static final int CARD_TIMEOUT_SEC = 15;

    /**
     * 获取校园卡基本信息页 Document（需已登录 ecard）
     */
    @CircuitBreaker(name = "cardSystem", fallbackMethod = "cardSystemFallback")
    public Document fetchCardBasicInfoDocument(String sessionId) throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CARD_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(ECARD_BASE + "/CardManage/CardInfo/BasicInfo");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("支付管理平台系统异常");
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) try { httpClient.close(); } catch (IOException ignored) { }
            if (cookieStore != null) HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
        }
    }

    /**
     * 获取消费流水页（当日 type=0 或历史 type=1 单页）
     *
     * @param type      0=当日，1=历史
     * @param pageIndex 页码，从 1 开始
     */
    @CircuitBreaker(name = "cardSystem", fallbackMethod = "cardSystemFallback")
    public Document fetchCardTrjnListDocument(String sessionId, int type, int pageIndex) throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CARD_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            String url = ECARD_BASE + "/CardManage/CardInfo/TrjnList?type=" + type;
            if (pageIndex > 1) url = url + "&pageindex=" + pageIndex;
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("支付管理平台系统异常");
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) try { httpClient.close(); } catch (IOException ignored) { }
            if (cookieStore != null) HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
        }
    }

    /**
     * 获取历史日期消费流水单页
     */
    @CircuitBreaker(name = "cardSystem", fallbackMethod = "cardSystemFallback")
    public Document fetchCardTrjnListByDateDocument(String sessionId, int year, int month, int date, int pageIndex) throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CARD_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            String d = year + "-" + month + "-" + date;
            String url = ECARD_BASE + "/CardManage/CardInfo/TrjnList?beginTime=" + d + "&endTime=" + d + "&type=1";
            if (pageIndex > 1) url = url + "&pageindex=" + pageIndex;
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("支付管理平台系统异常");
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) try { httpClient.close(); } catch (IOException ignored) { }
            if (cookieStore != null) HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
        }
    }

    /**
     * 挂失流程：拉取挂失页 Document（POST LossCard needHeader=false）
     */
    @CircuitBreaker(name = "cardSystem", fallbackMethod = "cardSystemFallback")
    public Document fetchLossCardPageDocument(String sessionId) throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CARD_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpPost httpPost = new HttpPost(ECARD_BASE + "/CardManage/CardInfo/LossCard");
            List<BasicNameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("needHeader", "false"));
            httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("支付管理平台系统异常");
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) try { httpClient.close(); } catch (IOException ignored) { }
            if (cookieStore != null) HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
        }
    }

    /**
     * 挂失流程：获取安全键盘图片（返回字节数组，避免连接关闭后流不可读）
     */
    @CircuitBreaker(name = "cardSystem", fallbackMethod = "cardSystemFallbackBytes")
    public byte[] fetchKeyPadImage(String sessionId) throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CARD_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(ECARD_BASE + "/Account/GetNumKeyPadImg");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("支付管理平台系统异常");
            }
            return readBytes(httpResponse.getEntity().getContent());
        } finally {
            if (httpClient != null) try { httpClient.close(); } catch (IOException ignored) { }
            if (cookieStore != null) HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
        }
    }

    /**
     * 挂失流程：获取验证码图片（relativePath 如 /Account/GetCheckCodeImg?xxx）
     */
    @CircuitBreaker(name = "cardSystem", fallbackMethod = "cardSystemFallbackBytes")
    public byte[] fetchCheckcodeImage(String sessionId, String relativePath) throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CARD_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            String url = relativePath.startsWith("http") ? relativePath : ECARD_BASE + relativePath;
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("支付管理平台系统异常");
            }
            return readBytes(httpResponse.getEntity().getContent());
        } finally {
            if (httpClient != null) try { httpClient.close(); } catch (IOException ignored) { }
            if (cookieStore != null) HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
        }
    }

    private static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        int n;
        while ((n = in.read(b)) != -1) buf.write(b, 0, n);
        return buf.toByteArray();
    }

    /**
     * 挂失流程：提交挂失请求，返回响应 JSON 字符串（含 ret、msg）
     */
    @CircuitBreaker(name = "cardSystem", fallbackMethod = "cardSystemFallbackString")
    public String submitSetCardLost(String sessionId, String passwordMapped, String checkCode)
            throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CARD_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpPost httpPost = new HttpPost(ECARD_BASE + "/CardManage/CardInfo/SetCardLost");
            List<BasicNameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("password", passwordMapped));
            form.add(new BasicNameValuePair("checkCode", checkCode));
            httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
            httpPost.setHeader("Referer", ECARD_BASE + "/");
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("支付管理平台系统异常");
            }
            return EntityUtils.toString(httpResponse.getEntity());
        } finally {
            if (httpClient != null) try { httpClient.close(); } catch (IOException ignored) { }
            if (cookieStore != null) HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
        }
    }

    // ---- Circuit breaker fallback methods ----

    private Document cardSystemFallback(String sessionId, Throwable t) throws ServerErrorException {
        throw new ServerErrorException("一卡通系统暂时不可用，请稍后再试");
    }

    private Document cardSystemFallback(String sessionId, int type, int pageIndex,
                                        Throwable t) throws ServerErrorException {
        throw new ServerErrorException("一卡通系统暂时不可用，请稍后再试");
    }

    private Document cardSystemFallback(String sessionId, int year, int month, int date, int pageIndex,
                                        Throwable t) throws ServerErrorException {
        throw new ServerErrorException("一卡通系统暂时不可用，请稍后再试");
    }

    private byte[] cardSystemFallbackBytes(String sessionId, Throwable t) throws ServerErrorException {
        throw new ServerErrorException("一卡通系统暂时不可用，请稍后再试");
    }

    private byte[] cardSystemFallbackBytes(String sessionId, String relativePath,
                                           Throwable t) throws ServerErrorException {
        throw new ServerErrorException("一卡通系统暂时不可用，请稍后再试");
    }

    private String cardSystemFallbackString(String sessionId, String passwordMapped, String checkCode,
                                            Throwable t) throws ServerErrorException {
        throw new ServerErrorException("一卡通系统暂时不可用，请稍后再试");
    }
}
