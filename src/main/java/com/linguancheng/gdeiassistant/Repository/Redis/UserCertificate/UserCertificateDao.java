package com.linguancheng.gdeiassistant.Repository.Redis.UserCertificate;

import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;

public interface UserCertificateDao {

    public UserCertificate queryUserCertificate(String username);

    public void saveUserCertificate(UserCertificate user);
}
