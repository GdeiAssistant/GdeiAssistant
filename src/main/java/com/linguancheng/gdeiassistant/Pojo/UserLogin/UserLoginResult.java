package com.gdeiassistant.gdeiassistant.Pojo.UserLogin;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.Token;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.User;

public class UserLoginResult {

    private User user;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
