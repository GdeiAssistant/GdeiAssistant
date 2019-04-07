package edu.gdei.gdeiassistant.Repository.Redis.AccessToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class AccessTokenDaoImpl implements AccessTokenDao {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 查询微信公众号全局唯一AccessToken
     *
     * @return
     */
    @Override
    public String QueryWechatAccessToken() {
        return redisTemplate.opsForValue().get("WechatAccessToken");
    }

    /**
     * 保存微信公众号全局唯一AccessToken
     *
     * @param accessToken
     */
    @Override
    public void SaveWechatAccessToken(String accessToken) {
        redisTemplate.opsForValue().set("WechatAccessToken", accessToken);
        redisTemplate.expire("WechatAccessToken", 7200, TimeUnit.SECONDS);
    }
}
