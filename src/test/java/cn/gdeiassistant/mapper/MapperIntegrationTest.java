package cn.gdeiassistant.mapper;

import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.topic.pojo.entity.TopicEntity;
import cn.gdeiassistant.core.charge.mapper.ChargeOrderMapper;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderStatus;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mapper integration tests using H2 in-memory database.
 * Verifies SQL syntax and column-to-field mapping correctness.
 */
class MapperIntegrationTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setUp() throws Exception {
        InputStream config = Resources.getResourceAsStream("mybatis-test-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            Connection conn = session.getConnection();
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-mappertest.sql"));
            Statement stmt = conn.createStatement();
            stmt.execute("INSERT INTO ershou (username,name,description,price,location,type,state) VALUES ('testuser','测试商品','测试描述',99.9,'A栋',0,1)");
            stmt.execute("INSERT INTO lostandfound (username,name,description,location,item_type,lost_type,state) VALUES ('testuser','测试物品','丢了','B栋',0,0,0)");
            stmt.execute("INSERT INTO express (username,nickname,self_gender,name,content,person_gender) VALUES ('testuser','小明',0,'小红','表白内容',1)");
            stmt.execute("INSERT INTO delivery_order (username,name,number,phone,price,company,address,state) VALUES ('testuser','张三','12345678901','13800138000',5.00,'顺丰','C栋',0)");
            stmt.execute("INSERT INTO secret_content (username,content,theme,type,timer,state) VALUES ('testuser','匿名内容',1,0,0,0)");
            stmt.execute("INSERT INTO topic (username,topic,content,count) VALUES ('testuser','校园话题','讨论内容',0)");
            stmt.execute("INSERT INTO charge_order (order_id,username,amount,status,created_at,updated_at,check_count,version) VALUES ('synthetic-order-old','testuser',30,'PROCESSING','2026-04-28 10:00:00','2026-04-28 10:00:00',0,0)");
            stmt.execute("INSERT INTO charge_order (order_id,username,amount,status,created_at,updated_at,check_count,version) VALUES ('synthetic-order-new','testuser',50,'PAYMENT_SESSION_CREATED','2026-04-28 11:00:00','2026-04-28 11:00:00',0,0)");
            stmt.execute("INSERT INTO charge_order (order_id,username,amount,status,created_at,updated_at,check_count,version) VALUES ('synthetic-other-order','otheruser',20,'PAYMENT_SESSION_CREATED','2026-04-28 12:00:00','2026-04-28 12:00:00',0,0)");
            stmt.close();
            session.commit();
        }
    }

    @Test
    void marketplaceSelectAvailableItems() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MarketplaceMapper mapper = session.getMapper(MarketplaceMapper.class);
            List<MarketplaceItemEntity> items = mapper.selectAvailableItems(0, 10);
            assertNotNull(items);
            assertFalse(items.isEmpty());
            MarketplaceItemEntity item = items.get(0);
            assertEquals("testuser", item.getUsername());
            assertEquals("测试商品", item.getName());
        }
    }

    @Test
    void marketplaceSelectByUsername() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MarketplaceMapper mapper = session.getMapper(MarketplaceMapper.class);
            List<MarketplaceItemEntity> items = mapper.selectItemsByUsername("testuser");
            assertNotNull(items);
            assertFalse(items.isEmpty());
        }
    }

    @Test
    void marketplaceKeywordSearch() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MarketplaceMapper mapper = session.getMapper(MarketplaceMapper.class);
            List<MarketplaceItemEntity> items = mapper.selectItemsWithKeyword(0, 10, "测试");
            assertNotNull(items);
            assertFalse(items.isEmpty());
        }
    }

    @Test
    void marketplaceKeywordSearchNoInjection() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MarketplaceMapper mapper = session.getMapper(MarketplaceMapper.class);
            List<MarketplaceItemEntity> items = mapper.selectItemsWithKeyword(0, 10, "'; DROP TABLE ershou; --");
            assertNotNull(items);
            assertTrue(items.isEmpty());
        }
    }

    @Test
    void lostAndFoundSelectByUsername() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LostAndFoundMapper mapper = session.getMapper(LostAndFoundMapper.class);
            List<LostAndFoundItemEntity> items = mapper.selectItemByUsername("testuser");
            assertNotNull(items);
            assertFalse(items.isEmpty());
            assertEquals("测试物品", items.get(0).getName());
        }
    }

    @Test
    void expressSelectPage() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ExpressMapper mapper = session.getMapper(ExpressMapper.class);
            List<ExpressEntity> items = mapper.selectExpress(0, 10, "testuser");
            assertNotNull(items);
            assertFalse(items.isEmpty());
            assertEquals("小明", items.get(0).getNickname());
        }
    }

    @Test
    void deliverySelectByUsername() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DeliveryMapper mapper = session.getMapper(DeliveryMapper.class);
            List<DeliveryOrderEntity> items = mapper.selectDeliveryOrderByUsername("testuser");
            assertNotNull(items);
            assertFalse(items.isEmpty());
            assertEquals("张三", items.get(0).getName());
        }
    }

    @Test
    void secretSelectPage() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SecretMapper mapper = session.getMapper(SecretMapper.class);
            List<SecretContentEntity> items = mapper.selectSecret(0, 10);
            assertNotNull(items);
            assertFalse(items.isEmpty());
            assertEquals("匿名内容", items.get(0).getContent());
        }
    }

    @Test
    void topicSelectPage() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TopicMapper mapper = session.getMapper(TopicMapper.class);
            List<TopicEntity> items = mapper.selectTopicPage(0, 10, "testuser");
            assertNotNull(items);
            assertFalse(items.isEmpty());
            assertEquals("校园话题", items.get(0).getTopic());
        }
    }

    @Test
    void chargeOrderSelectByOwner() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ChargeOrderMapper mapper = session.getMapper(ChargeOrderMapper.class);
            ChargeOrderEntity order = mapper.findByOrderIdAndUsername("synthetic-order-new", "testuser");
            ChargeOrderEntity otherUserOrder = mapper.findByOrderIdAndUsername("synthetic-order-new", "otheruser");

            assertNotNull(order);
            assertEquals("synthetic-order-new", order.getOrderId());
            assertEquals("testuser", order.getUsername());
            assertEquals(50, order.getAmount());
            assertEquals(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name(), order.getStatus());
            assertNull(otherUserOrder);
        }
    }

    @Test
    void chargeOrderSelectRecentByStatus() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ChargeOrderMapper mapper = session.getMapper(ChargeOrderMapper.class);
            List<ChargeOrderEntity> items = mapper.findRecentByUsernameAndStatus("testuser",
                    ChargeOrderStatus.PAYMENT_SESSION_CREATED.name(), 0, 10);

            assertEquals(1, items.size());
            assertEquals("synthetic-order-new", items.get(0).getOrderId());
            assertEquals(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name(), items.get(0).getStatus());
        }
    }
}
