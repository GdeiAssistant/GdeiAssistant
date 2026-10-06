package cn.gdeiassistant.integration.httpclient;

import cn.gdeiassistant.common.tools.utils.StringUtils;
import cn.gdeiassistant.common.redis.cookiestore.CookieStoreDao;
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

@Component
public class HttpClientUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private CookieStoreDao cookieStoreDao;
    private final PoolingHttpClientConnectionManager connectionManager = createConnectionManager();

    @Autowired
    public void setCookieStoreDao(CookieStoreDao cookieStoreDao) {
        this.cookieStoreDao = cookieStoreDao;
    }

    @jakarta.annotation.PreDestroy
    public void closePool() {
        connectionManager.close();
    }

    /**
     * 使用 JVM 默认信任库进行证书验证，仅允许 TLSv1.2 和 TLSv1.3
     *
     * @return
     */
    private static PoolingHttpClientConnectionManager createConnectionManager() {
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
            PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(registry);
            pool.setMaxTotal(80);
            pool.setDefaultMaxPerRoute(20);
            pool.setValidateAfterInactivity(5000);
            return pool;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize pooled HTTP client", e);
        }
    }

    /**
     * 同步CookieStore
     *
     * @param sessionId
     * @param cookieStore
     */
    public void syncHttpClientCookieStore(String sessionId, CookieStore cookieStore) {
        if (StringUtils.isNotBlank(sessionId)) {
            cookieStoreDao.saveCookieStore(sessionId, cookieStore);
        }
    }

    /**
     * 清空CookieStore
     *
     * @param sessionId
     */
    public void clearHttpClientCookieStore(String sessionId) {
        if (StringUtils.isNotBlank(sessionId)) {
            cookieStoreDao.clearCookieStore(sessionId);
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
    public HttpClientSession getHttpClient(String sessionId, boolean automaticRedirect, int timeOut) {
        timeOut = timeOut * 1000;
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        //设置超时时间
        requestConfigBuilder.setSocketTimeout(timeOut).setConnectTimeout(timeOut)
                .setConnectionRequestTimeout(timeOut);
        //配置默认请求配置
        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
        //设置连接管理器
        httpClientBuilder.setConnectionManager(connectionManager).setConnectionManagerShared(true).disableAutomaticRetries();
        //设置自动重定向配置
        if (!automaticRedirect) {
            httpClientBuilder.disableRedirectHandling();
        }
        //配置CookieStore
        CookieStore cookieStore = new BasicCookieStore();
        if (StringUtils.isNotBlank(sessionId)) {
            cookieStore = cookieStoreDao.queryCookieStore(sessionId);
            if (cookieStore == null) {
                cookieStore = new BasicCookieStore();
            }
        }
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        return new HttpClientSession(httpClientBuilder.build(), cookieStore);
    }

}
