package com.gdeiassistant.gdeiassistant.Repository.Redis.UserCertificate;

import com.gdeiassistant.gdeiassistant.Pojo.UserLogin.UserCertificate;

public interface UserCertificateDao {

    public UserCertificate queryUserCertificate(String username);

    public void saveUserCertificate(UserCertificate user);
}
