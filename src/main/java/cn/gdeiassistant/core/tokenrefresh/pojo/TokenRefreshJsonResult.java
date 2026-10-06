package cn.gdeiassistant.core.tokenrefresh.pojo;

import cn.gdeiassistant.common.pojo.entity.Token;
import cn.gdeiassistant.common.pojo.result.JsonResult;

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
