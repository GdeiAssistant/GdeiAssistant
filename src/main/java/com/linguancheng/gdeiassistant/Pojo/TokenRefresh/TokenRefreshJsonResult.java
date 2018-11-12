package com.linguancheng.gdeiassistant.Pojo.TokenRefresh;

import com.linguancheng.gdeiassistant.Pojo.Entity.Token;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/6
 */
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
