package cn.gdeiassistant.Pojo.TokenRefresh;

import cn.gdeiassistant.Pojo.Entity.Token;
import cn.gdeiassistant.Pojo.Result.JsonResult;

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
