package com.linguancheng.gdeiassistant.Pojo.TokenRefresh;

import com.linguancheng.gdeiassistant.Enum.Base.TokenValidResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.AccessToken;
import com.linguancheng.gdeiassistant.Pojo.Entity.RefreshToken;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/6
 */
public class TokenRefreshResult {

    private AccessToken accessToken;

    private RefreshToken refreshToken;

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }
}
