package cn.gdeiassistant.Repository.Redis.UserCertificate;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.UserLogin.UserCertificate;

public interface UserCertificateDao {

    public User queryUserLoginCertificate(String sessionId);

    public void saveUserLoginCertificate(String session, String username, String password);

    public void updateUserLoginCertificateExpiration(String sessionId);

    public UserCertificate queryUserSessionCertificate(String sessionId);

    public void saveUserSessionCertificate(String sessionId, UserCertificate user);

}
