package cn.gdeiassistant.integration.httpclient;

import cn.gdeiassistant.common.redis.cookiestore.CookieStoreDao;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpClientUtilsTest {
    @Test void freshAuthenticationCookiesAreIsolatedAndClosingOneClientKeepsPoolUsable() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var receivedCookie = new AtomicReference<String>();
        server.createContext("/", exchange -> {
            receivedCookie.set(exchange.getRequestHeaders().getFirst("Cookie"));
            exchange.getResponseHeaders().set("Set-Cookie", "synthetic=session; Path=/");
            exchange.sendResponseHeaders(200, 0); exchange.getResponseBody().close(); exchange.close();
        });
        server.start(); var pool = new HttpClientUtils();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            var first = pool.getHttpClient(null, false, 2);
            try (var client = first.getCloseableHttpClient(); var response = client.execute(new HttpGet(url))) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals(1, first.getCookieStore().getCookies().size());
            }
            var second = pool.getHttpClient(null, false, 2);
            try (var client = second.getCloseableHttpClient(); var response = client.execute(new HttpGet(url))) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertNull(receivedCookie.get());
                assertNotSame(first.getCookieStore(), second.getCookieStore());
            }
            var dao = mock(CookieStoreDao.class); pool.setCookieStoreDao(dao);
            var cached = new BasicCookieStore(); var cookie = new BasicClientCookie("synthetic", "cached");
            cookie.setDomain("127.0.0.1"); cookie.setPath("/"); cached.addCookie(cookie);
            when(dao.queryCookieStore("session")).thenReturn(cached);
            var restored = pool.getHttpClient("session", true, 2);
            try (var client = restored.getCloseableHttpClient(); var response = client.execute(new HttpGet(url))) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals("synthetic=cached", receivedCookie.get());
            }
            pool.syncHttpClientCookieStore("session", cached); pool.clearHttpClientCookieStore("session");
            verify(dao).saveCookieStore("session", cached); verify(dao).clearCookieStore("session");
        } finally { pool.closePool(); server.stop(0); }
    }
}
