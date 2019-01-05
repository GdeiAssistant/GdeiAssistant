package com.linguancheng.gdeiassistant.Tools;

import com.linguancheng.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import com.linguancheng.gdeiassistant.Repository.Redis.CookieStore.CookieStoreDao;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpSessionRequiredException;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpSession;

/**
 * Created by linguancheng on 2017/7/17.
 */
@Component
public class HttpClientUtils {

    private static String proxyIP;

    private static Integer proxyPort;

    private static String proxyType;

    private static CookieStoreDao cookieStoreDao;

    @Autowired
    public void setCookieStoreDao(CookieStoreDao cookieStoreDao) {
        HttpClientUtils.cookieStoreDao = cookieStoreDao;
    }

    @Value("#{propertiesReader['client.proxy.ip']}")
    public void setProxyIP(String proxyIP) {
        HttpClientUtils.proxyIP = proxyIP;
    }

    @Value("#{propertiesReader['client.proxy.port']}")
    public void setProxyPort(Integer proxyPort) {
        HttpClientUtils.proxyPort = proxyPort;
    }

    @Value("#{propertiesReader['client.proxy.type']}")
    public void setProxyType(String proxyType) {
        HttpClientUtils.proxyType = proxyType;
    }

    /**
     * 忽略服务器证书，采用信任策略
     *
     * @return
     */
    private static HttpClientConnectionManager getHttpClientConnectionManager() {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null,
                    (arg0, arg1) -> true).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                    new String[]{"TLSv1"}, null,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Registry registry = RegistryBuilder.create().register("http",
                    PlainConnectionSocketFactory.INSTANCE).
                    register("https", sslsf).build();
            return new PoolingHttpClientConnectionManager(registry);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 同步CookieStore
     *
     * @param sessionId
     * @param cookieStore
     */
    public static void SyncHttpClientCookieStore(String sessionId, CookieStore cookieStore) {
        cookieStoreDao.SaveCookieStore(sessionId, cookieStore);
    }

    /**
     * 清空CookieStore
     *
     * @param sessionId
     */
    public static void ClearHttpClientCookieStore(String sessionId) {
        cookieStoreDao.ClearCookieStore(sessionId);
    }

    /**
     * 获取HttpClientSession对象
     *
     * @param sessionId
     * @param automaticRedirect
     * @param timeOut
     * @return
     */
    public static HttpClientSession getHttpClient(String sessionId, boolean automaticRedirect, int timeOut) {
        timeOut = timeOut * 1000;
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        //设置超时时间
        requestConfigBuilder.setSocketTimeout(timeOut).setConnectTimeout(timeOut)
                .setConnectionRequestTimeout(timeOut);
        //自动检测StaleConnection
        requestConfigBuilder.setStaleConnectionCheckEnabled(true);
        //配置默认请求配置
        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
        //设置连接管理器
        httpClientBuilder.setConnectionManager(getHttpClientConnectionManager());
        //设置自动重定向配置
        if (!automaticRedirect) {
            httpClientBuilder.disableRedirectHandling();
        }
        //配置CookieStore
        CookieStore cookieStore = null;
        if (StringUtils.isNotBlank(sessionId)) {
            cookieStore = cookieStoreDao.QueryCookieStore(sessionId);
            if (cookieStore == null) {
                cookieStore = new BasicCookieStore();
            }
            httpClientBuilder.setDefaultCookieStore(cookieStore);
        }
        return new HttpClientSession(httpClientBuilder.build(), cookieStore);
    }

    /**
     * 获取携带CookieStore的CloseableHttpClient对象
     *
     * @param httpSession
     * @param automaticRedirect
     * @param timeOut
     * @return
     * @throws HttpSessionRequiredException
     */
    @Deprecated
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
                            .setConnectionManager(getHttpClientConnectionManager())
                            .setDefaultCookieStore((CookieStore) httpSession
                                    .getAttribute("cookieStore")).build();
                }
                return HttpClients.custom().disableRedirectHandling()
                        .setDefaultRequestConfig(RequestConfig.custom().
                                setSocketTimeout(timeOut).setConnectTimeout(timeOut).
                                setConnectionRequestTimeout(timeOut).setStaleConnectionCheckEnabled(true).build())
                        .setConnectionManager(getHttpClientConnectionManager())
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
                            .setConnectionManager(getHttpClientConnectionManager())
                            .setDefaultCookieStore(cookieStore)
                            .build();
                }
                return HttpClients.custom().disableRedirectHandling()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setSocketTimeout(timeOut).setConnectTimeout(timeOut)
                                .setConnectionRequestTimeout(timeOut).setStaleConnectionCheckEnabled(true).build())
                        .setConnectionManager(getHttpClientConnectionManager())
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
    @Deprecated
    public void ClearCookies(HttpSession httpSession) {
        if (httpSession != null) {
            if (httpSession.getAttribute("cookieStore") != null) {
                httpSession.removeAttribute("cookieStore");
            }
        }
    }
}
