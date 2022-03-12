package cn.gdeiassistant.Tools.SpringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisDaoUtils {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public <T> T get(String key) {
        if (redisTemplate != null) {
            return (T) redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    public <E> void set(String key, E object) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, object);
        }
    }

    public void delete(String key) {
        if (redisTemplate != null) {
            redisTemplate.delete(key);
        }
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        if (redisTemplate != null) {
            redisTemplate.expire(key, timeout, unit);
        }
    }
}
