package com.linguancheng.gdeiassistant.Factory;

import okhttp3.OkHttpClient;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpSessionRequiredException;

import javax.servlet.http.HttpSession;

/**
 * Created by linguancheng on 2017/7/17.
 */

@Component
@Scope("singleton")
public class HttpClientFactory {

    private String proxyIP;

    private Integer proxyPort;

    private String proxyType;

    @Value("#{propertiesReader['client.proxy.ip']}")
    public void setProxyIP(String proxyIP) {
        this.proxyIP = proxyIP;
    }

    @Value("#{propertiesReader['client.proxy.port']}")
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    @Value("#{propertiesReader['client.proxy.type']}")
    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    @Autowired
    private HttpClientConnectionManagerFactory httpClientConnectionManagerFactory;

    /**
     * 获取携带CookieStore的CloseableHttpClient对象
     *
     * @param httpSession
     * @param automaticRedirect
     * @param timeOut
     * @return
     * @throws HttpSessionRequiredException
     */
    public CloseableHttpClient getHttpClient(HttpSession httpSession
            , boolean automaticRedirect, int timeOut) throws HttpSessionRequiredException {
        timeOut = timeOut * 1000;
        if (httpSession != null) {
            if (httpSession.getAttribute("cookieStore") != null) {
                if (automaticRedirect) {
                    return HttpClients.custom()
                            .setDefaultRequestConfig(RequestConfig.custom().
                                    setSocketTimeout(timeOut).setConnectTimeout(timeOut).
                                    setConnectionRequestTimeout(timeOut).setStaleConnectionCheckEnabled(true).build())
                            .setConnectionManager(httpClientConnectionManagerFactory.getHttpClientConnectionManager())
                            .setDefaultCookieStore((CookieStore) httpSession
                                    .getAttribute("cookieStore")).build();
                }
                return HttpClients.custom().disableRedirectHandling()
                        .setDefaultRequestConfig(RequestConfig.custom().
                                setSocketTimeout(timeOut).setConnectTimeout(timeOut).
                                setConnectionRequestTimeout(timeOut).setStaleConnectionCheckEnabled(true).build())
                        .setConnectionManager(httpClientConnectionManagerFactory.getHttpClientConnectionManager())
                        .setDefaultCookieStore((CookieStore) httpSession
                                .getAttribute("cookieStore")).build();
            } else {
                //Session中没有CookieStore,在Session中创建新的CookieStore并在构造HttpClient将其添入
                CookieStore cookieStore = new BasicCookieStore();
                httpSession.setAttribute("cookieStore", cookieStore);
                if (automaticRedirect) {
                    return HttpClients.custom()
                            .setDefaultRequestConfig(RequestConfig.custom()
                                    .setSocketTimeout(timeOut).setConnectTimeout(timeOut)
                                    .setConnectionRequestTimeout(timeOut).setStaleConnectionCheckEnabled(true).build())
                            .setConnectionManager(httpClientConnectionManagerFactory.getHttpClientConnectionManager())
                            .setDefaultCookieStore(cookieStore)
                            .build();
                }
                return HttpClients.custom().disableRedirectHandling()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setSocketTimeout(timeOut).setConnectTimeout(timeOut)
                                .setConnectionRequestTimeout(timeOut).setStaleConnectionCheckEnabled(true).build())
                        .setConnectionManager(httpClientConnectionManagerFactory.getHttpClientConnectionManager())
                        .setDefaultCookieStore(cookieStore)
                        .build();
            }
        }
        throw new HttpSessionRequiredException("Session不存在，用户可能禁用了Cookie");
    }

    /**
     * 清除用户登录记录和Cookie缓存
     *
     * @param httpSession
     */
    public void ClearCookies(HttpSession httpSession) {
        if (httpSession != null) {
            if (httpSession.getAttribute("cookieStore") != null) {
                httpSession.removeAttribute("cookieStore");
            }
        }
    }
}
