package cn.gdeiassistant.common.redis.CookieStore;

import cn.gdeiassistant.common.tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import org.apache.http.client.CookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Repository
public class CookieStoreDaoImpl implements CookieStoreDao {

    private final String PREFIX = "COOKIE_STORE_";

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    /**
     * 存储CookieStore
     *
     * @param sessionId
     * @param cookieStore
     */
    @Override
    public void SaveCookieStore(String sessionId, CookieStore cookieStore) {
        String key = StringEncryptUtils.sha256HexString(PREFIX + sessionId);
        redisDaoUtils.setSerializable(key, (Serializable) cookieStore);
        redisDaoUtils.expire(key, 1, TimeUnit.HOURS);
    }

    @Override
    public CookieStore QueryCookieStore(String sessionId) {
        return redisDaoUtils.getSerializable(StringEncryptUtils.sha256HexString(PREFIX + sessionId));
    }

    @Override
    public void ClearCookieStore(String sessionId) {
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(PREFIX + sessionId));
    }
}
