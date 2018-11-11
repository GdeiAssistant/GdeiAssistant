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

    private String accessToken;

    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
