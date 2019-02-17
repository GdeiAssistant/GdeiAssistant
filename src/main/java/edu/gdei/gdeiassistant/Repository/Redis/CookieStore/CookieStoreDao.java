package edu.gdei.gdeiassistant.Repository.Redis.CookieStore;

import org.apache.http.client.CookieStore;

public interface CookieStoreDao {

    public void SaveCookieStore(String sessionId, CookieStore cookieStore);

    public CookieStore QueryCookieStore(String sessionId);

    public void ClearCookieStore(String sessionId);
}
