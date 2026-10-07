package cn.gdeiassistant.common.tools.springutils;

import cn.gdeiassistant.common.config.redis.RedisConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="GDEI_REDIS_TEST_PORT", matches="\\d+")
class RedisAtomicConsumptionTest {
    LettuceConnectionFactory factory;
    RedisTemplate<String,String> redis;
    RedisDaoUtils utils;
    String key = "synthetic-audit:" + java.util.UUID.randomUUID();
    @BeforeEach void setup() {
        factory = new LettuceConnectionFactory("127.0.0.1", Integer.parseInt(System.getenv("GDEI_REDIS_TEST_PORT")));
        factory.afterPropertiesSet(); factory.start();
        redis = new RedisConfig().redisTemplate(factory);
        utils = new RedisDaoUtils(); ReflectionTestUtils.setField(utils,"redisTemplate",redis);
    }
    @AfterEach void close() { try { redis.delete(key); } finally { factory.destroy(); } }
    @Test void wrongGuessDoesNotConsumeAndConcurrentCorrectGuessesSucceedOnce() throws Exception {
        utils.set(key,"123456",300,TimeUnit.SECONDS);
        assertFalse(utils.compareAndDelete(key,"654321")); assertEquals("123456",utils.get(key));
        var start = new CountDownLatch(1); var pool = Executors.newFixedThreadPool(2);
        try {
            var a=pool.submit(() -> { start.await(); return utils.compareAndDelete(key,"123456"); });
            var b=pool.submit(() -> { start.await(); return utils.compareAndDelete(key,"123456"); });
            start.countDown(); assertEquals(1,(a.get(5,TimeUnit.SECONDS)?1:0)+(b.get(5,TimeUnit.SECONDS)?1:0));
            assertNull(utils.get(key));
        } finally {pool.shutdownNow();}
    }
    @Test void serializedValueAndExpiryAreWrittenTogether() {
        utils.setSerializable(key,new java.util.ArrayList<>(java.util.List.of("synthetic-cookie")),30,TimeUnit.SECONDS);
        assertEquals(java.util.List.of("synthetic-cookie"),utils.getSerializable(key));
        assertTrue(redis.getExpire(key,TimeUnit.SECONDS)>0); assertTrue(redis.getExpire(key,TimeUnit.SECONDS)<=30);
    }
    @Test void unconfiguredStoreNeverAcceptsACode() { assertFalse(new RedisDaoUtils().compareAndDelete(key,"123456")); }
}
