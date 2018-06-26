package com.linguancheng.gdeiassistant.Factory;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;

/**
 * Created by linguancheng on 2017/7/22.
 */

@Component
@Scope("singleton")

/**
 *  忽略服务器证书，采用信任机制
 */
public class HttpClientConnectionManagerFactory {

    public HttpClientConnectionManager getHttpClientConnectionManager() {
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
}
