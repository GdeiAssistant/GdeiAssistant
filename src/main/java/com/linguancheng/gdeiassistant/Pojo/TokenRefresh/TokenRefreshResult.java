package com.gdeiassistant.gdeiassistant.Pojo.TokenRefresh;

import com.gdeiassistant.gdeiassistant.Enum.Base.TokenValidResultEnum;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.AccessToken;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.RefreshToken;

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
