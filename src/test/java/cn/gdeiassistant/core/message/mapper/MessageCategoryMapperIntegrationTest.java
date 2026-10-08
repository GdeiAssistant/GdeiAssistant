package cn.gdeiassistant.core.message.mapper;
import cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class MessageCategoryMapperIntegrationTest {
 private SqlSession session;
 @BeforeEach void setup() throws Exception {
  var source = new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
  var db = new JdbcTemplate(source);
  db.execute("CREATE TABLE interaction_notification(notification_id BIGINT PRIMARY KEY,module VARCHAR(32),receiver_username VARCHAR(16),is_read INT,create_time TIMESTAMP)");
  db.update("INSERT INTO interaction_notification VALUES(1,'delivery','alice',0,now()),(2,'topic','alice',0,now()),(3,'delivery','bob',0,now()),(4,'topic','alice',1,now())");
  db.execute("CREATE TABLE announcement(id INT PRIMARY KEY,title VARCHAR(80),content VARCHAR(80),publish_time TIMESTAMP)");
  db.execute("CREATE TABLE announcement_read(username VARCHAR(16),announcement_id INT,PRIMARY KEY(username,announcement_id))");
  db.update("INSERT INTO announcement VALUES(1,'one','text',now()),(2,'two','text',now())");
  var config = new XMLConfigBuilder(getClass().getResourceAsStream("/mybatis-config.xml")).parse();
  config.setEnvironment(new Environment("messages",new JdbcTransactionFactory(),source));
  config.addMapper(InteractionNotificationMapper.class); config.addMapper(AnnouncementMapper.class);
  session = new SqlSessionFactoryBuilder().build(config).openSession(true);
 }
 @AfterEach void close() { session.close(); }
 @Test void categoriesPartitionUnreadAndReadOnlyTheirRecipient() {
  var mapper=session.getMapper(InteractionNotificationMapper.class);
  assertEquals(1,mapper.countCategoryUnread("alice",true)); assertEquals(1,mapper.countCategoryUnread("alice",false));
  assertEquals(2,mapper.selectUnreadInteractionNotificationCount("alice"));
  assertEquals(1,mapper.selectCategoryPage("alice",true,0,20).size());
  assertEquals(2,mapper.selectCategoryPage("alice",false,0,20).size());
  mapper.markCategoryRead("alice",true);
  assertEquals(0,mapper.countCategoryUnread("alice",true)); assertEquals(1,mapper.countCategoryUnread("alice",false));
  assertEquals(1,mapper.countCategoryUnread("bob",true));
  assertEquals(0,mapper.updateInteractionNotificationRead("bob",2L));
 }
 @Test void announcementReceiptsArePerUserIdempotentAndIgnoreMissingIds() {
  var mapper=session.getMapper(AnnouncementMapper.class);
  assertEquals(2,mapper.countUnread("alice"));
  mapper.markRead("alice",1); mapper.markRead("alice",1); mapper.markRead("alice",999);
  assertEquals(1,mapper.countUnread("alice")); assertEquals(2,mapper.countUnread("bob"));
  mapper.deleteUserReads("alice"); assertEquals(2,mapper.countUnread("alice"));
 }
}
