package com.linguancheng.gdeiassistant.Repository.Redis.User;

import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/10/21
 */


public interface UserDao {

    public UserCertificate queryUserCertificate(String username);

    public void saveUserCertificate(UserCertificate user);
}
