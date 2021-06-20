package cn.gdeiassistant.Repository.Redis.CookieStore;

import cn.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.http.client.CookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Repository
public class CookieStoreDaoImpl implements CookieStoreDao {

    private final String PREFIX = "COOKIE_STORE_";

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
        redisTemplate.opsForValue().set(StringEncryptUtils.SHA256HexString(PREFIX + sessionId)
                , (Serializable) cookieStore);
        redisTemplate.expire(sessionId, 1, TimeUnit.HOURS);
    }

    /**
     * 获取CookieStore
     *
     * @param sessionId
     * @return
     */
    @Override
    public CookieStore QueryCookieStore(String sessionId) {
        return (CookieStore) redisTemplate.opsForValue().get(StringEncryptUtils
                .SHA256HexString(PREFIX + sessionId));
    }

    @Override
    public void ClearCookieStore(String sessionId) {
        redisTemplate.delete(StringEncryptUtils.SHA256HexString(PREFIX + sessionId));
    }
}
