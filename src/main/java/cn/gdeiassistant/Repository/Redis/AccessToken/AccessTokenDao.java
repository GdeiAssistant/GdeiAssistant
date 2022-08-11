package cn.gdeiassistant.Repository.Redis.AccessToken;

public interface AccessTokenDao {

    String QueryWechatAccessToken();

    void SaveWechatAccessToken(String accessToken);
}
