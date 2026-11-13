package cn.gdeiassistant.integration.cas;

import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import cn.gdeiassistant.common.i18n.Messages;

/**
 * CAS 认证防腐层：负责 security.gdei.edu.cn 的登录表单提交，仅返回封装了 Cookie 的 CasSessionCredential。
 */
@Component
public class CasClient {

    private final String CAS_LOGIN_URL;

    public CasClient(@Value("${campus.upstream.cas-login-url:https://security.gdei.edu.cn/cas/login}") String CAS_LOGIN_URL) {
        this.CAS_LOGIN_URL = CAS_LOGIN_URL;
    }
    private static final int TIMEOUT_MS = 10000;

    private static final Set<String> ALLOWED_REDIRECT_HOSTS = Set.of(
            "security.gdei.edu.cn",
            "jwgl.gdei.edu.cn",
            "portal.gdei.edu.cn",
            "epay.gdei.edu.cn"
    );

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
                throw new ServerErrorException(Messages.get("integration.cas.loginPageFailed"));
            }
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));

            // 2. 提交登录表单
            Element tokensElement = document.getElementById("tokens");
            Element stampElement = document.getElementById("stamp");
            if (tokensElement == null || stampElement == null) {
                throw new ServerErrorException(Messages.get("userlogin.casPageTokensMissing"));
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
                throw new ServerErrorException(Messages.get("integration.cas.loginSubmitFailed"));
            }
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));

            // 3. 解析跳转到业务系统的链接（web.gdei.edu.cn 入口）
            Element firstLink = document.select("a").first();
            if (firstLink == null) {
                throw new ServerErrorException(Messages.get("userlogin.casRedirectMissing"));
            }
            String redirectUrl = firstLink.attr("href");
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                throw new ServerErrorException(Messages.get("userlogin.casRedirectEmpty"));
            }
            // Validate redirect URL stays within trusted school domains
            try {
                URI uri = URI.create(redirectUrl);
                String scheme = uri.getScheme();
                if (scheme != null && !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                    throw new ServerErrorException(Messages.get("userlogin.casRedirectProtocolUnsafe"));
                }
                String host = uri.getHost();
                if (host == null || !ALLOWED_REDIRECT_HOSTS.contains(host.toLowerCase())) {
                    throw new ServerErrorException(Messages.get("userlogin.casRedirectHostUntrusted"));
                }
            } catch (IllegalArgumentException e) {
                throw new ServerErrorException(Messages.get("userlogin.casRedirectMalformed"));
            }

            CasSessionCredential credential = new CasSessionCredential();
            credential.setCookieStore(cookieStore);
            credential.setServiceUrl(redirectUrl);
            return credential;
        }
    }
}

