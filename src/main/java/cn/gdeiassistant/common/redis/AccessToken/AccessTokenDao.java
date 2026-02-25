package cn.gdeiassistant.common.redis.AccessToken;

public interface AccessTokenDao {

    String QueryWechatAccessToken();

    void SaveWechatAccessToken(String accessToken);
}
