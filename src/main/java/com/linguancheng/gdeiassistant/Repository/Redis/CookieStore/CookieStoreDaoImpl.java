package com.linguancheng.gdeiassistant.Repository.Redis.CookieStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.CookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/12
 */

@Repository
public class CookieStoreDaoImpl implements CookieStoreDao {

    private Log log = LogFactory.getLog(CookieStoreDao.class);

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    /**
     * 存储CookieStore
     *
     * @param sessionId
     * @param cookieStore
     */
    @Override
    public void SaveCookieStore(String sessionId, CookieStore cookieStore) {
        try {
            redisTemplate.opsForValue().set(sessionId, (Serializable) cookieStore);
            redisTemplate.expire(sessionId, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("存储CookieStore异常：", e);
        }
    }

    /**
     * 获取CookieStore
     *
     * @param sessionId
     * @return
     */
    @Override
    public CookieStore QueryCookieStore(String sessionId) {
        CookieStore cookieStore = null;
        try {
            cookieStore = (CookieStore) redisTemplate.opsForValue().get(sessionId);
        } catch (Exception e) {
            log.error("获取CookieStore异常：", e);
        }
        return cookieStore;
    }

    @Override
    public void ClearCookieStore(String sessionId) {
        try {
            redisTemplate.delete(sessionId);
        } catch (Exception e) {
            log.error("清除CookieStore异常：", e);
        }
    }


}
