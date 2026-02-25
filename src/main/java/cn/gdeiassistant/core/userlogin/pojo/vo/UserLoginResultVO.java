package cn.gdeiassistant.core.userLogin.pojo.vo;

import cn.gdeiassistant.common.pojo.Entity.Token;
import cn.gdeiassistant.common.pojo.Entity.User;

import java.io.Serializable;

/**
 * 登录结果视图：User + accessToken + refreshToken（仅做类型包装，不改变 Token 签发逻辑）。
 */
public class UserLoginResultVO implements Serializable {

    private User user;
    private Token accessToken;
    private Token refreshToken;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Token getAccessToken() { return accessToken; }
    public void setAccessToken(Token accessToken) { this.accessToken = accessToken; }
    public Token getRefreshToken() { return refreshToken; }
    public void setRefreshToken(Token refreshToken) { this.refreshToken = refreshToken; }
}
