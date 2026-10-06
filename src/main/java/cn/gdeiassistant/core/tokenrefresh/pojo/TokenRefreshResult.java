package cn.gdeiassistant.core.tokenrefresh.pojo;

import cn.gdeiassistant.common.pojo.entity.AccessToken;
import cn.gdeiassistant.common.pojo.entity.RefreshToken;

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
