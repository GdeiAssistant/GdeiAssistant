package com.gdeiassistant.gdeiassistant.Pojo.TokenRefresh;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.Token;
import com.gdeiassistant.gdeiassistant.Pojo.Result.JsonResult;

public class TokenRefreshJsonResult extends JsonResult {

    private Token accessToken;

    private Token refreshToken;

    public Token getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(Token accessToken) {
        this.accessToken = accessToken;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(Token refreshToken) {
        this.refreshToken = refreshToken;
    }
}
