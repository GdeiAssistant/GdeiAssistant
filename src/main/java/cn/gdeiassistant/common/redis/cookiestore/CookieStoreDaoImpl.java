package cn.gdeiassistant.common.redis.cookiestore;

import cn.gdeiassistant.common.tools.springutils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
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
    public void saveCookieStore(String sessionId, CookieStore cookieStore) {
        String key = StringEncryptUtils.sha256HexString(PREFIX + sessionId);
        redisDaoUtils.setSerializable(key, (Serializable) cookieStore, 1, TimeUnit.HOURS);
    }

    @Override
    public CookieStore queryCookieStore(String sessionId) {
        return redisDaoUtils.getSerializable(StringEncryptUtils.sha256HexString(PREFIX + sessionId));
    }

    @Override
    public void clearCookieStore(String sessionId) {
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(PREFIX + sessionId));
    }
}
