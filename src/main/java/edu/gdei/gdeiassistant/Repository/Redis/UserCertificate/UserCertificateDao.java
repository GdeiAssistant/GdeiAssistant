package edu.gdei.gdeiassistant.Repository.Redis.UserCertificate;

import edu.gdei.gdeiassistant.Pojo.UserLogin.UserCertificate;

public interface UserCertificateDao {

    public UserCertificate queryUserCertificate(String username);

    public void saveUserCertificate(UserCertificate user);
}
