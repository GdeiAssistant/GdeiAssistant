package cn.gdeiassistant.Repository.Redis.CookieStore;

import cn.gdeiassistant.Tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
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
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(PREFIX + sessionId)
                , (Serializable) cookieStore);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(PREFIX + sessionId), 1, TimeUnit.HOURS);
    }

    /**
     * 获取CookieStore
     *
     * @param sessionId
     * @return
     */
    @Override
    public CookieStore QueryCookieStore(String sessionId) {
        return redisDaoUtils.get(StringEncryptUtils.SHA256HexString(PREFIX + sessionId));
    }

    @Override
    public void ClearCookieStore(String sessionId) {
        redisDaoUtils.delete(StringEncryptUtils.SHA256HexString(PREFIX + sessionId));
    }
}
