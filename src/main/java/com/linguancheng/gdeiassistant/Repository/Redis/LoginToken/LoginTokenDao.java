package com.linguancheng.gdeiassistant.Repository.Redis.LoginToken;

import com.linguancheng.gdeiassistant.Pojo.Entity.AccessToken;
import com.linguancheng.gdeiassistant.Pojo.Entity.RefreshToken;

public interface LoginTokenDao {

    public String QueryToken(String signature);

    public Boolean InsertAccessToken(AccessToken token);

    public Boolean InsertRefreshToken(RefreshToken token);

    public Boolean UpdateAccessToken(AccessToken token);

    public Boolean DeleteToken(String signature);
}
