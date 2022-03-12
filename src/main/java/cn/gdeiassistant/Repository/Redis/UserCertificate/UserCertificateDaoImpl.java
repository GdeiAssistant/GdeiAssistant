package cn.gdeiassistant.Repository.Redis.UserCertificate;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.UserLogin.UserCertificate;
import cn.gdeiassistant.Tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class UserCertificateDaoImpl implements UserCertificateDao {

    private final String COOKIE_PREFIX = "USER_COOKIE_CERTIFICATE_";

    private final String LOGIN_PREFIX = "USER_LOGIN_CERTIFICATE_";

    private final String SESSION_PREFIX = "USER_SESSION_CERTIFICATE_";

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    @Override
    public User queryUserCookieCertificate(String cookieId) {
        Map<String, String> map = redisDaoUtils.get(StringEncryptUtils.SHA256HexString(COOKIE_PREFIX
                + cookieId));
        if (map != null) {
            User user = new User();
            user.setUsername(map.get("username"));
            user.setPassword(map.get("password"));
            return user;
        }
        return null;
    }

    @Override
    public void saveUserCookieCertificate(String cookieId, String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(COOKIE_PREFIX + cookieId), map);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(COOKIE_PREFIX + cookieId)
                , 30, TimeUnit.DAYS);
    }

    @Override
    public void updateUserCookieCertificateExpiration(String cookieId) {
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(COOKIE_PREFIX
                + cookieId), 30, TimeUnit.DAYS);
    }

    @Override
    public User queryUserLoginCertificate(String sessionId) {
        Map<String, String> map = redisDaoUtils.get(StringEncryptUtils.SHA256HexString(LOGIN_PREFIX
                + sessionId));
        if (map != null) {
            User user = new User();
            user.setUsername(map.get("username"));
            user.setPassword(map.get("password"));
            return user;
        }
        return null;
    }

    @Override
    public void updateUserLoginCertificateExpiration(String sessionId) {
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(LOGIN_PREFIX
                + sessionId), 1, TimeUnit.HOURS);
    }

    @Override
    public void saveUserLoginCertificate(String sessionId, String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(LOGIN_PREFIX + sessionId), map);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(LOGIN_PREFIX + sessionId)
                , 1, TimeUnit.HOURS);
    }

    @Override
    public UserCertificate queryUserSessionCertificate(String sessionId) {
        Map<String, String> map = redisDaoUtils.get(StringEncryptUtils
                .SHA256HexString(SESSION_PREFIX + sessionId));
        if (map != null) {
            UserCertificate userCertificate = new UserCertificate();
            User user = new User();
            user.setUsername(map.get("username"));
            user.setPassword(map.get("password"));
            userCertificate.setUser(user);
            userCertificate.setKeycode(map.get("keycode"));
            userCertificate.setNumber(map.get("number"));
            userCertificate.setTimestamp(Long.valueOf(map.get("timestamp")));
            return userCertificate;
        }
        return null;
    }

    @Override
    public void saveUserSessionCertificate(String sessionId, UserCertificate userCertificate) {
        Map<String, String> map = new HashMap<>();
        String username = userCertificate.getUser().getUsername();
        String password = userCertificate.getUser().getPassword();
        String keycode = userCertificate.getKeycode();
        String number = userCertificate.getNumber();
        Long timestamp = userCertificate.getTimestamp();
        map.put("username", username);
        map.put("password", password);
        map.put("keycode", keycode);
        map.put("number", number);
        map.put("timestamp", String.valueOf(timestamp));
        redisDaoUtils.set(StringEncryptUtils
                .SHA256HexString(SESSION_PREFIX + sessionId), map);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(SESSION_PREFIX + sessionId)
                , 30, TimeUnit.MINUTES);
    }
}
