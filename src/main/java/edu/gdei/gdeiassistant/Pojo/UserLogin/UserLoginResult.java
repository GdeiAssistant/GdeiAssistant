package edu.gdei.gdeiassistant.Pojo.UserLogin;

import edu.gdei.gdeiassistant.Pojo.Entity.Token;
import edu.gdei.gdeiassistant.Pojo.Entity.User;

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
