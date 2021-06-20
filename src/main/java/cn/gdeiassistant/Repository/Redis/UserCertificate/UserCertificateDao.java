package cn.gdeiassistant.Repository.Redis.UserCertificate;

import cn.gdeiassistant.Pojo.UserLogin.UserCertificate;

public interface UserCertificateDao {

    public UserCertificate queryUserCertificate(String username);

    public void saveUserCertificate(UserCertificate user);
}
