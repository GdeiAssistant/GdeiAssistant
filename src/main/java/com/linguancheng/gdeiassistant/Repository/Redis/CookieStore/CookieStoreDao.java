package com.linguancheng.gdeiassistant.Repository.Redis.CookieStore;

import org.apache.http.client.CookieStore;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/12
 */

public interface CookieStoreDao {

    public void SaveCookieStore(String sessionId, CookieStore cookieStore);

    public CookieStore QueryCookieStore(String sessionId);

    public void ClearCookieStore(String sessionId);
}
