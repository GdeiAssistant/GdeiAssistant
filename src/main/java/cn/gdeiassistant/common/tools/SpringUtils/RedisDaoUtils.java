package cn.gdeiassistant.common.tools.SpringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 统一使用 RedisConfig 提供的 redisTemplate（StringRedisSerializer），
 * 仅支持字符串 Value，对象由调用方序列化为 JSON 后写入。
 */
@Component
public class RedisDaoUtils {

    @Autowired(required = false)
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    public String get(String key) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    public void set(String key, String value) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value);
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

    /** 存 Java 序列化后 Base64 字符串，用于 CookieStore 等非 JSON 友好对象，与 StringRedisSerializer 一致 */
    public void setSerializable(String key, java.io.Serializable value) {
        if (redisTemplate == null || value == null) return;
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos)) {
                oos.writeObject(value);
            }
            set(key, java.util.Base64.getEncoder().encodeToString(baos.toByteArray()));
        } catch (java.io.IOException e) {
            throw new RuntimeException("Redis 序列化失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getSerializable(String key) {
        String b64 = get(key);
        if (b64 == null || b64.isEmpty()) return null;
        try {
            byte[] bytes = java.util.Base64.getDecoder().decode(b64);
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes))) {
                return (T) ois.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Redis 反序列化失败", e);
        }
    }
}
