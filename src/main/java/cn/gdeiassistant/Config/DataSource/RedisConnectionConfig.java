package cn.gdeiassistant.Config.DataSource;

import cn.gdeiassistant.Enum.Module.CoreModuleEnum;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
     * 开发环境RedisTemplate
     *
     * @return
     */
    @Bean(name = "redisTemplate")
    @Profile("development")
    public RedisTemplate developmentRedisTemplate() {
        String host = environment.getProperty("redis.dev.host");
        String port = environment.getProperty("redis.dev.port");
        String pass = environment.getProperty("redis.dev.pass");
        String maxIdle = environment.getProperty("redis.maxIdle");
        String maxWait = environment.getProperty("redis.maxWait");
        String testOnBorrow = environment.getProperty("redis.testOnBorrow");
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(port) && StringUtils.isNotBlank(pass) && StringUtils.isNumeric(port)
                && StringUtils.isNotBlank(host) && StringUtils.isNotBlank(port) && StringUtils.isNotBlank(pass)
                && StringUtils.isNumeric(port)) {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(Integer.parseInt(maxIdle));
            jedisPoolConfig.setMaxWaitMillis(Long.parseLong(maxWait));
            //配置Redis客户端连接参数
            JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration
                    .builder().usePooling().poolConfig(jedisPoolConfig).build();
            //配置Redis单机连接配置
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            redisStandaloneConfiguration.setHostName(host);
            redisStandaloneConfiguration.setPort(Integer.parseInt(port));
            redisStandaloneConfiguration.setPassword(pass);
            JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration
                    , jedisClientConfiguration);
            RedisTemplate redisTemplate = new RedisTemplate();
            redisTemplate.setConnectionFactory(jedisConnectionFactory);
            return redisTemplate;
        }
        moduleUtils.DisableCoreModule(CoreModuleEnum.REDIS);
        return null;
    }

    /**
     * 生产环境RedisTemplate
     *
     * @return
     */
    @Bean(name = "redisTemplate")
    @Profile("production")
    public RedisTemplate productionRedisTemplate() {
        String host = environment.getProperty("redis.pro.host");
        String port = environment.getProperty("redis.pro.port");
        String pass = environment.getProperty("redis.pro.pass");
        String maxIdle = environment.getProperty("redis.maxIdle");
        String maxWait = environment.getProperty("redis.maxWait");
        String testOnBorrow = environment.getProperty("redis.testOnBorrow");
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(port) && StringUtils.isNotBlank(pass) && StringUtils.isNumeric(port)
                && StringUtils.isNotBlank(host) && StringUtils.isNotBlank(port) && StringUtils.isNotBlank(pass)
                && StringUtils.isNumeric(port)) {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(Integer.parseInt(maxIdle));
            jedisPoolConfig.setMaxWaitMillis(Long.parseLong(maxWait));
            //配置Redis客户端连接参数
            JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration
                    .builder().usePooling().poolConfig(jedisPoolConfig).build();
            //配置Redis单机连接配置
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            redisStandaloneConfiguration.setHostName(host);
            redisStandaloneConfiguration.setPort(Integer.parseInt(port));
            redisStandaloneConfiguration.setPassword(pass);
            JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration
                    , jedisClientConfiguration);
            RedisTemplate redisTemplate = new RedisTemplate();
            redisTemplate.setConnectionFactory(jedisConnectionFactory);
            return redisTemplate;
        }
        moduleUtils.DisableCoreModule(CoreModuleEnum.REDIS);
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
