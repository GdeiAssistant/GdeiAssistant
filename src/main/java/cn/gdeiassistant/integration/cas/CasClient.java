package cn.gdeiassistant.integration.cas;

import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.integration.cas.pojo.CasSessionCredential;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CAS 认证防腐层：负责 security.gdei.edu.cn 的登录表单提交，仅返回封装了 Cookie 的 CasSessionCredential。
 */
@Component
public class CasClient {

    private static final String CAS_LOGIN_URL = "https://security.gdei.edu.cn/cas/login";
    private static final int TIMEOUT_MS = 10000;

    public CasSessionCredential login(String username, String password, String serviceUrl)
            throws IOException, ServerErrorException {
        CookieStore cookieStore = new BasicCookieStore();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MS)
                .setSocketTimeout(TIMEOUT_MS)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            // 1. 获取登录页面，解析 tokens / stamp
            HttpGet httpGet = new HttpGet(CAS_LOGIN_URL);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("CAS 登录页访问异常");
            }
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));

            // 2. 提交登录表单
            Element tokensElement = document.getElementById("tokens");
            Element stampElement = document.getElementById("stamp");
            if (tokensElement == null || stampElement == null) {
                throw new ServerErrorException("CAS 登录页面结构异常，未找到 tokens 或 stamp");
            }
            HttpPost httpPost = new HttpPost(CAS_LOGIN_URL);
            List<BasicNameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("service", serviceUrl));
            form.add(new BasicNameValuePair("username", username));
            form.add(new BasicNameValuePair("password", password));
            form.add(new BasicNameValuePair("imageField.x", "0"));
            form.add(new BasicNameValuePair("imageField.y", "0"));
            form.add(new BasicNameValuePair("tokens", tokensElement.val()));
            form.add(new BasicNameValuePair("stamp", stampElement.val()));
            httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("CAS 登录提交异常");
            }
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));

            // 3. 解析跳转到业务系统的链接（web.gdei.edu.cn 入口）
            Element firstLink = document.select("a").first();
            if (firstLink == null) {
                throw new ServerErrorException("CAS 登录后未找到跳转链接");
            }
            String redirectUrl = firstLink.attr("href");
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                throw new ServerErrorException("CAS login后跳转链接为空");
            }
            // Validate redirect URL stays within trusted school domains
            try {
                java.net.URI uri = java.net.URI.create(redirectUrl);
                String host = uri.getHost();
                if (host != null && !host.toLowerCase().endsWith(".gdei.edu.cn") && !host.toLowerCase().equals("gdei.edu.cn")) {
                    throw new ServerErrorException("CAS重定向地址不在可信域名范围内");
                }
            } catch (IllegalArgumentException e) {
                throw new ServerErrorException("CAS重定向地址格式异常");
            }

            CasSessionCredential credential = new CasSessionCredential();
            credential.setCookieStore(cookieStore);
            credential.setServiceUrl(redirectUrl);
            return credential;
        }
    }
}

