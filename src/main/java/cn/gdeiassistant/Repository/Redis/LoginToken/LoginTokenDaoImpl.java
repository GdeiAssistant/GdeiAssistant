package cn.gdeiassistant.Repository.Redis.LoginToken;

import cn.gdeiassistant.Pojo.Entity.AccessToken;
import cn.gdeiassistant.Pojo.Entity.RefreshToken;
import cn.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class LoginTokenDaoImpl implements LoginTokenDao {

    private final String PREFIX = "LOGIN_TOKEN_";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 查找令牌签名对应的记录信息
     *
     * @param signature
     * @return
     */
    @Override
    public String QueryToken(String signature) {
        return redisTemplate.opsForValue().get(StringEncryptUtils.SHA256HexString(PREFIX + signature));
    }

    /**
     * 插入权限令牌信息
     *
     * @param token
     */
    @Override
    public void InsertAccessToken(AccessToken token) {
        //保存权限令牌，设置有效期为7天
        redisTemplate.opsForValue().set(StringEncryptUtils.SHA256HexString(PREFIX + token.getSignature()), token.getIp());
        redisTemplate.expire(StringEncryptUtils.SHA256HexString(PREFIX + token.getSignature()), 7, TimeUnit.DAYS);
    }

    /**
     * 插入刷新令牌信息
     *
     * @param token
     */
    @Override
    public void InsertRefreshToken(RefreshToken token) {
        //保存刷新令牌，设置有效期为30天
        redisTemplate.opsForValue().set(StringEncryptUtils.SHA256HexString(PREFIX + token.getSignature())
                , token.getTokenSignature());
        redisTemplate.expire(StringEncryptUtils.SHA256HexString(PREFIX + token.getSignature())
                , 30, TimeUnit.DAYS);
    }

    @Override
    public void UpdateAccessToken(AccessToken token) {
        //更新权限令牌信息，但不重新设置有效期
        redisTemplate.opsForValue().set(StringEncryptUtils.SHA256HexString(PREFIX + token.getSignature()), token.getIp());
    }

    /**
     * 删除令牌
     *
     * @param signature
     * @return
     */
    @Override
    public Boolean DeleteToken(String signature) {
        return redisTemplate.expire(StringEncryptUtils.SHA256HexString(PREFIX + signature), 5, TimeUnit.MINUTES);
    }

}
