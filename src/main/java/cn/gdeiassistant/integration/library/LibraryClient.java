package cn.gdeiassistant.integration.library;

import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.integration.httpclient.HttpClientSession;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import cn.gdeiassistant.integration.library.pojo.LibraryRenewResult;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图书馆网络防腐层：agentdockingopac.featurelib.libsou.com、m.5read.com / mc.m.5read.com，
 * 仅负责 HTTP 请求与响应解析为 Document/DTO，不包含 CAS。
 */
@Component
public class LibraryClient {

    private static final Logger logger = LoggerFactory.getLogger(LibraryClient.class);
    private static final String OPAC_RENEW_BASE = "http://agentdockingopac.featurelib.libsou.com";
    private static final String OPAC_SEARCH_BASE = "http://agentdockingopac.featurelib.libsou.com";
    private static final String FIVEREAD_M = "http://m.5read.com";
    private static final String FIVEREAD_MC = "http://mc.m.5read.com";
    private static final int LIBRARY_TIMEOUT_SEC = 15;
    private static final int OKHTTP_TIMEOUT_SEC = 10;

    /**
     * 续借图书（OPAC 续借接口，无 CAS）
     *
     * @param sessionId 用于 HttpClient/Cookie 同步
     * @param sn        续借参数 sn
     * @param code      续借参数 code
     * @return 续借结果 result + message
     */
    public LibraryRenewResult renewBook(String sessionId, String sn, String code) throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, LIBRARY_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(OPAC_RENEW_BASE + "/showhome/searchrenew/opacSearchRenew?&check=1&sn="
                    + sn + "&code=" + code + "&schoolId=705");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("图书馆系统异常");
            }
            JSONObject jsonObject = JSON.parseObject(EntityUtils.toString(httpResponse.getEntity()));
            LibraryRenewResult result = new LibraryRenewResult();
            result.setResult(jsonObject.getIntValue("result"));
            result.setMessage(jsonObject.getString("msg"));
            return result;
        } finally {
            closeHttpClient(httpClient);
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 移动图书馆登录并拉取借阅列表页 Document（5read，无 CAS）
     *
     * @param sessionId 用于 Cookie 同步
     * @param number    学号
     * @param password  图书馆密码
     * @return 借阅列表页 Jsoup Document（个人中心 opacLink 页），失败抛异常
     */
    public Document fetchBorrowedBooksPage(String sessionId, String number, String password)
            throws IOException, PasswordIncorrectException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, LIBRARY_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(FIVEREAD_M + "/705");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("图书馆系统异常");
            }
            httpGet = new HttpGet(FIVEREAD_MC + "/user/login/showLogin.jspx?backurl=/user/uc/showUserCenter.jspx");
            httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() != 200 || !document.title().equals("移动图书馆服务登录")) {
                throw new ServerErrorException("图书馆系统异常");
            }
            HttpPost httpPost = new HttpPost(FIVEREAD_MC + "/irdUser/login/opac/opacLogin.jspx");
            List<BasicNameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("schoolid", "705"));
            form.add(new BasicNameValuePair("backurl", "/user/uc/showUserCenter.jspx"));
            form.add(new BasicNameValuePair("userType", "0"));
            form.add(new BasicNameValuePair("username", number));
            form.add(new BasicNameValuePair("password", password));
            httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 302) {
                throw new ServerErrorException("图书馆系统异常");
            }
            httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("移动图书馆服务登录")) {
                throw new PasswordIncorrectException("图书馆查询密码错误");
            }
            if (httpResponse.getStatusLine().getStatusCode() != 200 || !document.title().equals("个人中心")) {
                throw new ServerErrorException("图书馆系统异常");
            }
            httpGet = new HttpGet(FIVEREAD_MC + "/cmpt/opac/opacLink.jspx?stype=1");
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("图书馆系统异常");
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            closeHttpClient(httpClient);
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 馆藏检索列表页（OPAC featurelib，无会话）
     *
     * @param page    页码，可为 1
     * @param keyword 关键词
     * @return 检索列表页 Jsoup Document
     */
    public Document fetchCollectionListPage(Integer page, String keyword) throws IOException, ServerErrorException {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
        String url = OPAC_SEARCH_BASE + "/showhome/searchlist/opacSearchList?search="
                + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                + "&xc=3&schoolId=705&centerDomain=&searchtype=title";
        if (page != null && page != 1) {
            url = url + "&page=" + page;
        }
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return parseSuccessfulResponse(response);
        }
    }

    /**
     * 馆藏详情页（OPAC featurelib，无会话）
     *
     * @param opacUrl    opacUrl
     * @param search     search
     * @param schoolId   schoolId，默认 705
     * @param searchtype searchtype，默认 title
     * @param page       page，默认 1
     * @param xc         xc，默认 3
     * @return 馆藏详情页 Jsoup Document
     */
    public Document fetchCollectionDetailPage(String opacUrl, String search, String schoolId,
                                               String searchtype, String page, String xc) throws IOException, ServerErrorException {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
        String u = OPAC_SEARCH_BASE + "/showhome/searchdetail/opacSearchDetail?opacUrl="
                + URLEncoder.encode(opacUrl, StandardCharsets.UTF_8)
                + "&search=" + URLEncoder.encode(search, StandardCharsets.UTF_8)
                + "&schoolId=" + URLEncoder.encode(schoolId != null ? schoolId : "705", StandardCharsets.UTF_8)
                + "&searchtype=" + URLEncoder.encode(searchtype != null ? searchtype : "title", StandardCharsets.UTF_8)
                + "&page=" + URLEncoder.encode(page != null ? page : "1", StandardCharsets.UTF_8)
                + "&xc=" + URLEncoder.encode(xc != null ? xc : "3", StandardCharsets.UTF_8);
        Request request = new Request.Builder().url(u).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return parseSuccessfulResponse(response);
        }
    }

    private static Document parseSuccessfulResponse(Response response) throws IOException, ServerErrorException {
        ResponseBody responseBody = response.body();
        if (!response.isSuccessful() || responseBody == null) {
            throw new ServerErrorException("移动图书馆系统异常");
        }
        return Jsoup.parse(responseBody.string());
    }

    private static void closeHttpClient(CloseableHttpClient httpClient) {
        if (httpClient == null) {
            return;
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.warn("关闭图书馆 HTTP 客户端失败", e);
        }
    }
}
