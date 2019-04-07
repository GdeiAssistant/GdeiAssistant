package edu.gdei.gdeiassistant.Repository.Redis.AccessToken;

public interface AccessTokenDao {

    public String QueryWechatAccessToken();

    public void SaveWechatAccessToken(String accessToken);
}
