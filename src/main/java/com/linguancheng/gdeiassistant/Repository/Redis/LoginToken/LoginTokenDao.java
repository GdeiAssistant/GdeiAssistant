package com.linguancheng.gdeiassistant.Repository.Redis.LoginToken;

import com.linguancheng.gdeiassistant.Pojo.Entity.AccessToken;
import com.linguancheng.gdeiassistant.Pojo.Entity.RefreshToken;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/10/28
 */
public interface LoginTokenDao {

    public String QueryToken(String signature);

    public Boolean InsertAccessToken(AccessToken token);

    public Boolean InsertRefreshToken(RefreshToken token);

    public Boolean UpdateAccessToken(AccessToken token);

    public Boolean DeleteToken(String signature);
}
