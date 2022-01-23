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

    private final String PREFIX = "USER_CERTIFICATE_";

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    @Override
    public UserCertificate queryUserCertificate(String username) {
        Map<String, String> map = redisDaoUtils.get(StringEncryptUtils
                .SHA256HexString(PREFIX + username));
        if (map != null) {
            UserCertificate userCertificate = new UserCertificate();
            User user = new User();
            user.setUsername(username);
            user.setPassword(map.get("password"));
            user.setKeycode(map.get("keycode"));
            user.setNumber(map.get("number"));
            userCertificate.setUser(user);
            userCertificate.setTimestamp(Long.valueOf(map.get("timestamp")));
            return userCertificate;
        }
        return null;
    }

    @Override
    public void saveUserCertificate(UserCertificate userCertificate) {
        Map<String, String> map = new HashMap<>();
        User user = userCertificate.getUser();
        map.put("password", user.getPassword());
        map.put("keycode", user.getKeycode());
        map.put("number", user.getNumber());
        map.put("timestamp", String.valueOf(userCertificate.getTimestamp()));
        redisDaoUtils.set(StringEncryptUtils
                .SHA256HexString(PREFIX + user.getUsername()), map);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(PREFIX + user.getUsername())
                , 1, TimeUnit.HOURS);
    }
}
