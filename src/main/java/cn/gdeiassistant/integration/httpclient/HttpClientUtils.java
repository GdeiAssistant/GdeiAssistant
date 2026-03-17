package cn.gdeiassistant.integration.httpclient;

import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.common.redis.CookieStore.CookieStoreDao;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpSessionRequiredException;

import javax.net.ssl.SSLContext;
import jakarta.servlet.http.HttpSession;

@Component
public class HttpClientUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private static CookieStoreDao cookieStoreDao;

    @Autowired
    public void setCookieStoreDao(CookieStoreDao cookieStoreDao) {
        HttpClientUtils.cookieStoreDao = cookieStoreDao;
    }

    /**
     * 使用 JVM 默认信任库进行证书验证，仅允许 TLSv1.2 和 TLSv1.3
     *
     * @return
     */
    private static HttpClientConnectionManager getHttpClientConnectionManager() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1.2", "TLSv1.3"}, null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", sslsf)
                    .build();
            return new PoolingHttpClientConnectionManager(registry);
        } catch (Exception e) {
            logger.error("创建 HttpClientConnectionManager 失败", e);
        }
        return new PoolingHttpClientConnectionManager();
    }

    /**
     * 同步CookieStore
     *
     * @param sessionId
     * @param cookieStore
     */
    public static void syncHttpClientCookieStore(String sessionId, CookieStore cookieStore) {
        if (StringUtils.isNotBlank(sessionId)) {
            cookieStoreDao.SaveCookieStore(sessionId, cookieStore);
        }
    }

    /**
     * 清空CookieStore
     *
     * @param sessionId
     */
    public static void clearHttpClientCookieStore(String sessionId) {
        if (StringUtils.isNotBlank(sessionId)) {
            cookieStoreDao.ClearCookieStore(sessionId);
        }
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
    public void clearCookies(HttpSession httpSession) {
        if (httpSession != null) {
            if (httpSession.getAttribute("cookieStore") != null) {
                httpSession.removeAttribute("cookieStore");
            }
        }
    }
}
