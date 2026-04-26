package cn.gdeiassistant.common.redis.UserCertificate;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;

public interface UserCertificateDao {

    User queryUserCookieCertificate(String cookieId);

    void saveUserCookieCertificate(String cookieId, String username, String password);

    void updateUserCookieCertificateExpiration(String cookieId);

    User queryUserLoginCertificate(String sessionId);

    void saveUserLoginCertificate(String session, String username, String password);

    void updateUserLoginCertificateExpiration(String sessionId);

    void deleteUserLoginCertificate(String sessionId);

    void deleteUserSessionCertificate(String sessionId);

    void deleteUserLoginCertificatesByUsername(String username);

    void deleteUserSessionCertificatesByUsername(String username);

    UserCertificateEntity queryUserSessionCertificate(String sessionId);

    void saveUserSessionCertificate(String sessionId, UserCertificateEntity user);

}
