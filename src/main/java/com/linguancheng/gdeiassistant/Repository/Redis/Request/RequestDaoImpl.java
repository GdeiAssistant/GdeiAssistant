package com.linguancheng.gdeiassistant.Repository.Redis.Request;

import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RequestDaoImpl implements RequestDao {

    private Log log = LogFactory.getLog(RequestDao.class);

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
        try {
            String value = redisTemplate.opsForValue().get(StringEncryptUtils
                    .SHA256HexString(nonce));
            if (value != null) {
                return value;
            }
        } catch (Exception e) {
            log.error("查找请求记录异常：", e);
        }
        return null;
    }

    /**
     * 保存请求时间戳记录
     *
     * @param nonce
     * @param timestamp
     * @return
     */
    @Override
    public Boolean InsertRequest(String nonce, String timestamp) {
        try {
            //保存权限令牌，设置有效期为60秒
            redisTemplate.opsForValue().set(StringEncryptUtils
                    .SHA256HexString(nonce), timestamp);
            redisTemplate.expire(StringEncryptUtils
                    .SHA256HexString(nonce), 60, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.error("查找请求记录异常：", e);
            return false;
        }
    }
}
