package cn.gdeiassistant.integration.chsi;

import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.tools.Utils.ImageEncodeUtils;
import cn.gdeiassistant.integration.httpclient.HttpClientSession;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import okhttp3.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * 学信网/研招网网络防腐层：www.chsi.com.cn（四六级）、yz.chsi.com.cn（考研），
 * 仅负责 HTTP 请求与响应解析为 Document/Base64，不包含 CAS。
 */
@Component
public class ChsiClient {

    private static final String CET_BASE = "http://www.chsi.com.cn/cet";
    private static final String KAOYAN_CJCX = "https://yz.chsi.com.cn/apply/cjcx";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
    private static final String USER_AGENT_MAC = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36";
    private static final int CHSI_TIMEOUT_SEC = 15;
    private static final int OKHTTP_TIMEOUT_SEC = 5;

    /**
     * 四六级查询页 + 验证码图片（学信网，依赖 session Cookie）
     *
     * @param sessionId 用于 HttpClient/Cookie 同步
     * @return 验证码图片 Base64 字符串
     */
    public String fetchCetCaptchaImageBase64(String sessionId) throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CHSI_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(CET_BASE + "/");
            httpGet.setHeader("Referer", CET_BASE + "/");
            httpGet.setHeader("User-Agent", USER_AGENT);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("访问学信网异常");
            }
            httpGet = new HttpGet(CET_BASE + "/ValidatorIMG.JPG");
            httpGet.setHeader("Referer", CET_BASE + "/");
            httpGet.setHeader("User-Agent", USER_AGENT);
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("访问学信网异常");
            }
            return ImageEncodeUtils.convertToBase64(httpResponse.getEntity().getContent());
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
     * 四六级成绩查询页（学信网，依赖 session Cookie）
     *
     * @param sessionId 用于 Cookie
     * @param number    准考证号
     * @param name      姓名
     * @param checkcode 验证码
     * @return 查询结果页 Jsoup Document
     */
    public Document fetchCetQueryPage(String sessionId, String number, String name, String checkcode)
            throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, CHSI_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            String yzm = checkcode != null ? checkcode : "";
            HttpGet httpGet = new HttpGet(CET_BASE + "/query?zkzh=" + number + "&xm=" + name + "&yzm=" + yzm);
            httpGet.setHeader("Referer", CET_BASE + "/");
            httpGet.setHeader("User-Agent", USER_AGENT);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("学信网系统异常");
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
     * 考研成绩查询首页（研招网，无会话）
     *
     * @return 查询表单页 Jsoup Document
     */
    public Document fetchPostgraduateCjcxPage() throws IOException, ServerErrorException {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(KAOYAN_CJCX + "/").build();
        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new ServerErrorException("查询系统异常");
        }
        return Jsoup.parse(response.body().string());
    }

    /**
     * 考研验证码图片（研招网，无会话）
     *
     * @param imageUrl 完整 URL，如 https://yz.chsi.com.cn/...
     * @return 图片字节流，调用方可转 Base64
     */
    public InputStream fetchPostgraduateCaptchaImage(String imageUrl) throws IOException, ServerErrorException {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(imageUrl).build();
        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new ServerErrorException("查询系统异常");
        }
        return response.body().byteStream();
    }

    /**
     * 提交考研成绩查询（研招网，无会话）
     *
     * @param name       姓名
     * @param examNumber 考生编号
     * @param idNumber   证件号码
     * @param checkcode  验证码（可为空表示无需验证码）
     * @return 结果页 Jsoup Document
     */
    public Document submitPostgraduateQuery(String name, String examNumber, String idNumber, String checkcode)
            throws IOException, ServerErrorException {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(OKHTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
        FormBody.Builder builder = new FormBody.Builder()
                .add("xm", name)
                .add("zjhm", idNumber)
                .add("ksbh", examNumber)
                .add("ssdm", "")
                .add("bkdwdm", "");
        if (checkcode != null && !checkcode.isEmpty()) {
            builder.add("checkcode", checkcode);
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .post(requestBody)
                .url(KAOYAN_CJCX + "/cjcxAction.do")
                .addHeader("Referer", KAOYAN_CJCX + "/")
                .addHeader("User-Agent", USER_AGENT_MAC)
                .build();
        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new ServerErrorException("查询系统异常");
        }
        return Jsoup.parse(response.body().string());
    }
}
