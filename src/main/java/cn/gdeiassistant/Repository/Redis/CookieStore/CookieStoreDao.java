package cn.gdeiassistant.Repository.Redis.CookieStore;

import org.apache.http.client.CookieStore;

public interface CookieStoreDao {

    void SaveCookieStore(String sessionId, CookieStore cookieStore);

    CookieStore QueryCookieStore(String sessionId);

    void ClearCookieStore(String sessionId);
}
