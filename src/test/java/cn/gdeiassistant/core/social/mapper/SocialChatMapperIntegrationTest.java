package cn.gdeiassistant.core.social.mapper;

import cn.gdeiassistant.core.social.pojo.entity.ChatMessageEntity;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** SQL execution and MyBatis cache regressions; MySQL locking is verified separately. */
class SocialChatMapperIntegrationTest {
    private DriverManagerDataSource dataSource;
    private SqlSessionFactory sessions;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        Configuration configuration;
        try (InputStream input = getClass().getResourceAsStream("/mybatis-config.xml")) {
            configuration = new XMLConfigBuilder(input).parse();
        }
        configuration.setEnvironment(new Environment("social-regression", new JdbcTransactionFactory(), dataSource));
        configuration.addMapper(SocialChatMapper.class);
        sessions = new SqlSessionFactoryBuilder().build(configuration);
        execute("CREATE TABLE chat_message (id BIGINT AUTO_INCREMENT PRIMARY KEY, conversation_id BIGINT, "
                + "seq BIGINT, sender_id BIGINT, client_message_id CHAR(36), type VARCHAR(16) DEFAULT 'TEXT', "
                + "content VARCHAR(4000), image_key VARCHAR(255), image_content_type VARCHAR(64), "
                + "image_width INT, image_height INT, image_size INT, image_sha256 CHAR(64), created_at TIMESTAMP)");
    }

    private void execute(String sql) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    @Test
    void unreadQueryExecutesAndCountsOnlyPeerMessagesAfterReadCursor() throws Exception {
        execute("INSERT INTO chat_message(conversation_id,seq,sender_id,content) VALUES "
                + "(1,1,2,'read'),(1,2,1,'mine'),(1,3,2,'unread'),(2,4,2,'other conversation')");
        try (SqlSession session = sessions.openSession()) {
            SocialChatMapper mapper = session.getMapper(SocialChatMapper.class);
            assertEquals(2, mapper.countUnreadFromPeer(1, 2, 0));
            assertEquals(1, mapper.countUnreadFromPeer(1, 2, 1));
            assertEquals(0, mapper.countUnreadFromPeer(1, 2, 3));
        }
    }

    @Test
    void retryConfirmationSeesMessageCommittedAfterAnEarlierEmptyLookup() throws Exception {
        String clientId = UUID.randomUUID().toString();
        try (SqlSession session = sessions.openSession()) {
            SocialChatMapper mapper = session.getMapper(SocialChatMapper.class);
            assertNull(mapper.selectByClientMessageId(1, 2, clientId));
            // Separate committed connection models the original request finishing while a retry waits.
            execute("INSERT INTO chat_message(conversation_id,seq,sender_id,client_message_id,content) "
                    + "VALUES(1,1,2,'" + clientId + "','committed')");
            ChatMessageEntity message = mapper.selectByClientMessageId(1, 2, clientId);
            assertNotNull(message);
            assertEquals("committed", message.getContent());
        }
    }

    @Test
    void messagePaginationExecutesBothDirectionsWithoutIncludingBoundary() throws Exception {
        execute("INSERT INTO chat_message(conversation_id,seq,sender_id,content) VALUES "
                + "(1,1,2,'one'),(1,2,1,'two'),(1,3,2,'three')");
        try (SqlSession session = sessions.openSession()) {
            SocialChatMapper mapper = session.getMapper(SocialChatMapper.class);
            assertEquals(java.util.List.of(2L, 1L), mapper.selectMessages(1, 3L, null, 5)
                    .stream().map(ChatMessageEntity::getSeq).toList());
            assertEquals(java.util.List.of(2L, 3L), mapper.selectMessages(1, null, 1L, 5)
                    .stream().map(ChatMessageEntity::getSeq).toList());
        }
    }

    @Test
    void imageMetadataRoundTripAndNullTypeDefaultsReadable() throws Exception {
        String clientId = UUID.randomUUID().toString();
        execute("INSERT INTO chat_message(conversation_id,seq,sender_id,client_message_id,type,content,"
                + "image_key,image_content_type,image_width,image_height,image_size,image_sha256) VALUES "
                + "(1,1,2,'" + clientId + "','IMAGE','','chat/k','image/png',8,8,12,'abcd')");
        execute("INSERT INTO chat_message(conversation_id,seq,sender_id,client_message_id,content) VALUES "
                + "(1,2,2,'" + UUID.randomUUID() + "','legacy text')");
        try (SqlSession session = sessions.openSession()) {
            SocialChatMapper mapper = session.getMapper(SocialChatMapper.class);
            ChatMessageEntity image = mapper.selectByIdInConversation(1, 1);
            assertNotNull(image);
            assertEquals("IMAGE", image.getType());
            assertEquals("chat/k", image.getImageKey());
            assertEquals(Integer.valueOf(8), image.getImageWidth());
            ChatMessageEntity legacy = mapper.selectBySeq(1, 2);
            assertNotNull(legacy);
            assertEquals("TEXT", legacy.getType() == null ? "TEXT" : legacy.getType());
            assertNull(legacy.getImageKey());
        }
    }
}
