package cn.gdeiassistant.Config.DataSource;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
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

@Configuration
@PropertySource("classpath:/config/redis/redis.properties")
public class RedisConnectionConfig implements EnvironmentAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ModuleUtils moduleUtils;

    /**
     * 配置Redis参数
     *
     * @return
     */
    public JedisPoolConfig poolConfig() {
        String maxIdle = environment.getProperty("redis.maxIdle");
        String maxWait = environment.getProperty("redis.maxWait");
        String testOnBorrow = environment.getProperty("redis.testOnBorrow");
        if (StringUtils.isNotBlank(maxIdle) && StringUtils.isNotBlank(maxWait) && StringUtils.isNotBlank(testOnBorrow)
                && StringUtils.isNumeric(maxIdle) && StringUtils.isNumeric(maxWait)
                && StringUtils.isBoolean(testOnBorrow)) {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(Integer.parseInt(maxIdle));
            jedisPoolConfig.setMaxWaitMillis(Long.parseLong(maxWait));
            jedisPoolConfig.setTestOnBorrow(Boolean.parseBoolean(testOnBorrow));
            return jedisPoolConfig;
        }
        return null;
    }

    /**
     * 配置Redis连接工厂
     *
     * @return
     */
    public JedisConnectionFactory connectionFactory() {
        JedisPoolConfig poolConfig = poolConfig();
        if (poolConfig != null) {
            String host = environment.getProperty("redis.host");
            String port = environment.getProperty("redis.port");
            String pass = environment.getProperty("redis.pass");
            if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(port) && StringUtils.isNotBlank(pass)
                    && StringUtils.isNumeric(port)) {
                //配置Redis客户端连接参数
                JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration
                        .builder().usePooling().poolConfig(poolConfig).build();
                //配置Redis单机连接配置
                RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
                redisStandaloneConfiguration.setHostName(host);
                redisStandaloneConfiguration.setPort(Integer.parseInt(port));
                redisStandaloneConfiguration.setPassword(pass);
                //构造Redis连接工厂
                return new JedisConnectionFactory(redisStandaloneConfiguration
                        , jedisClientConfiguration);
            }
        }
        return null;
    }

    @Bean
    public RedisTemplate redisTemplate() {
        JedisConnectionFactory connectionFactory = connectionFactory();
        if (connectionFactory != null) {
            RedisTemplate redisTemplate = new RedisTemplate();
            redisTemplate.setConnectionFactory(connectionFactory());
            return redisTemplate;
        }
        moduleUtils.DisableModule(ModuleEnum.REDIS);
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
