package cn.gdeiassistant.common.redis.cookiestore;

import org.apache.http.client.CookieStore;

public interface CookieStoreDao {

    void saveCookieStore(String sessionId, CookieStore cookieStore);

    CookieStore queryCookieStore(String sessionId);

    void clearCookieStore(String sessionId);
}
