package cn.gdeiassistant.common.config.Redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 统一 Redis 序列化：Key 与 Value 均使用 StringRedisSerializer，
 * 避免 JDK 序列化与字符串序列化混用导致存取“暗号”不一致（如 loginToken→sessionId 查不到、401）。
 * 所有 Redis 操作通过同一 redisTemplate 实例，且 spring.redis.database 在各环境统一（默认 0）。
 */
@Configuration
public class RedisConfig {

    /** 唯一 Redis 模板：Key/Value 均字符串序列化，保证 loginToken→sessionId 等存取在物理层一致。 */
    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
