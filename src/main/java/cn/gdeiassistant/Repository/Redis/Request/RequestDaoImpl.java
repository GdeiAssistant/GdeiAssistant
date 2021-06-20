package cn.gdeiassistant.Repository.Redis.Request;

import cn.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RequestDaoImpl implements RequestDao {

    private final String PREFIX = "REQUEST_";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 通过随机数查找请求记录
     *
     * @param nonce
     * @return
     */
    @Override
    public String QueryRequest(String nonce) {
        return redisTemplate.opsForValue().get(StringEncryptUtils.SHA256HexString(PREFIX + nonce));
    }

    /**
     * 保存请求时间戳记录
     *
     * @param nonce
     * @param timestamp
     */
    @Override
    public void InsertRequest(String nonce, String timestamp) {
        //保存权限令牌，设置有效期为60秒
        redisTemplate.opsForValue().set(StringEncryptUtils.SHA256HexString(PREFIX + nonce), timestamp);
        redisTemplate.expire(StringEncryptUtils.SHA256HexString(PREFIX + nonce), 60, TimeUnit.SECONDS);
    }
}
