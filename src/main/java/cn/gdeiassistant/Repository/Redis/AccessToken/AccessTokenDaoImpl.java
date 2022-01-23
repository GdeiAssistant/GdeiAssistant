package cn.gdeiassistant.Repository.Redis.AccessToken;

import cn.gdeiassistant.Tools.SpringUtils.RedisDaoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class AccessTokenDaoImpl implements AccessTokenDao {

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    /**
     * 查询微信公众号全局唯一AccessToken
     *
     * @return
     */
    @Override
    public String QueryWechatAccessToken() {
        return redisDaoUtils.get("WechatAccessToken");
    }

    /**
     * 保存微信公众号全局唯一AccessToken
     *
     * @param accessToken
     */
    @Override
    public void SaveWechatAccessToken(String accessToken) {
        redisDaoUtils.get("WechatAccessToken");
        redisDaoUtils.expire("WechatAccessToken", 7200, TimeUnit.SECONDS);
    }
}
