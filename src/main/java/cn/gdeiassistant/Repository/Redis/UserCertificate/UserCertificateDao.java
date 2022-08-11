package cn.gdeiassistant.Repository.Redis.UserCertificate;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.UserLogin.UserCertificate;

public interface UserCertificateDao {

    User queryUserCookieCertificate(String cookieId);

    void saveUserCookieCertificate(String cookieId, String username, String password);

    void updateUserCookieCertificateExpiration(String cookieId);

    User queryUserLoginCertificate(String sessionId);

    void saveUserLoginCertificate(String session, String username, String password);

    void updateUserLoginCertificateExpiration(String sessionId);

    UserCertificate queryUserSessionCertificate(String sessionId);

    void saveUserSessionCertificate(String sessionId, UserCertificate user);

}
