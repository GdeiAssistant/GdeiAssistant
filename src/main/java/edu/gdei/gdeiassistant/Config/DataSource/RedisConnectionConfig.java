package edu.gdei.gdeiassistant.Config.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Objects;

@Configuration
@PropertySource("classpath:/config/redis/redis.properties")
public class RedisConnectionConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    /**
     * 配置Redis参数
     *
     * @return
     */
    public JedisPoolConfig poolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(Integer.parseInt(Objects.requireNonNull(environment.getProperty("redis.maxIdle"))));
        jedisPoolConfig.setMaxWaitMillis(Integer.parseInt(Objects.requireNonNull(environment.getProperty("redis.maxWait"))));
        jedisPoolConfig.setTestOnBorrow(Boolean.parseBoolean(environment.getProperty("redis.testOnBorrow")));
        return jedisPoolConfig;
    }

    /**
     * 配置Redis连接工厂
     *
     * @return
     */
    public JedisConnectionFactory connectionFactory() {
        //配置Redis客户端连接参数
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration
                .builder().usePooling().poolConfig(poolConfig()).build();
        //配置Redis单机连接配置
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(environment.getProperty("redis.host"));
        redisStandaloneConfiguration.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("redis.port"))));
        redisStandaloneConfiguration.setPassword(environment.getProperty("redis.pass"));
        //构造Redis连接工厂
        return new JedisConnectionFactory(redisStandaloneConfiguration
                , jedisClientConfiguration);
    }

    @Bean
    public RedisTemplate redisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory());
        return redisTemplate;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
