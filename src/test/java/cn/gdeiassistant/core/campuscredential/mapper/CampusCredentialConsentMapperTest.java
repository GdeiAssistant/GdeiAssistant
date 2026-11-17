package cn.gdeiassistant.core.campuscredential.mapper;

import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CampusCredentialConsentMapperTest {

    private UnpooledDataSource dataSource;
    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setUp() throws Exception {
        String dbName = "cc_mapper_" + UUID.randomUUID().toString().replace("-", "");
        dataSource = new UnpooledDataSource(
                "org.h2.Driver",
                "jdbc:h2:mem:" + dbName + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE app_user (id BIGINT PRIMARY KEY, public_id VARCHAR(36) NOT NULL)");
            stmt.execute("CREATE TABLE campus_credential (user_id BIGINT PRIMARY KEY, campus_username VARCHAR(24) NOT NULL UNIQUE, password VARCHAR(128))");
            stmt.execute("CREATE TABLE privacy (username VARCHAR(24) PRIMARY KEY, is_quick_auth_allow TINYINT(1) DEFAULT 0)");
            stmt.execute("CREATE TABLE campus_credential_consent (id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(24) NOT NULL, consent_type VARCHAR(64) NOT NULL, policy_date DATE, effective_date DATE, scene VARCHAR(32), consented_at TIMESTAMP, revoked_at TIMESTAMP, revoked_reason VARCHAR(64), create_time TIMESTAMP, update_time TIMESTAMP)");

            // 1. Eligible user with duplicate active consent rows -> should appear once.
            stmt.execute("INSERT INTO app_user (id, public_id) VALUES (1, 'p1'), (2, 'p2'), (3, 'p3'), (4, 'p4'), (5, 'p5'), (6, 'p6')");
            stmt.execute("INSERT INTO campus_credential (user_id, campus_username, password) VALUES " +
                    "(1, 'u_enabled', 'cipher_ok'), " +
                    "(2, 'u_revoked', 'cipher_ok'), " +
                    "(3, 'u_quick_off', 'cipher_ok'), " +
                    "(4, 'u_no_pass', NULL), " +
                    "(5, 'u_empty_pass', ''), " +
                    "(6, 'u_other_type', 'cipher_ok'), " +
                    "(99, 'u_no_app_user', 'cipher_ok')");
            stmt.execute("INSERT INTO privacy (username, is_quick_auth_allow) VALUES " +
                    "('u_enabled', 1), ('u_revoked', 1), ('u_quick_off', 0), ('u_no_pass', 1), ('u_empty_pass', 1), ('u_other_type', 1), ('u_no_app_user', 1)");
            stmt.execute("INSERT INTO campus_credential_consent (username, consent_type, revoked_at) VALUES " +
                    "('u_enabled', 'CAMPUS_CREDENTIAL', NULL), " +
                    "('u_enabled', 'CAMPUS_CREDENTIAL', NULL), " +
                    "('u_revoked', 'CAMPUS_CREDENTIAL', CURRENT_TIMESTAMP), " +
                    "('u_quick_off', 'CAMPUS_CREDENTIAL', NULL), " +
                    "('u_no_pass', 'CAMPUS_CREDENTIAL', NULL), " +
                    "('u_empty_pass', 'CAMPUS_CREDENTIAL', NULL), " +
                    "('u_other_type', 'OTHER_TYPE', NULL), " +
                    "('u_no_app_user', 'CAMPUS_CREDENTIAL', NULL)");
        }

        Environment environment = new Environment("test", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(CampusCredentialConsentMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SHUTDOWN");
        }
    }

    @Test
    void selectUsernamesWithEffectiveQuickAuthMatchesJoinedCriteria() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CampusCredentialConsentMapper mapper = session.getMapper(CampusCredentialConsentMapper.class);
            List<String> enabled = mapper.selectUsernamesWithEffectiveQuickAuth(
                    List.of("u_enabled", "u_revoked", "u_quick_off", "u_no_pass", "u_empty_pass", "u_other_type", "u_no_app_user", "u_missing"),
                    CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL
            );
            assertEquals(List.of("u_enabled"), enabled);
            assertTrue(mapper.selectUsernamesWithEffectiveQuickAuth(List.of(), CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL).isEmpty());
        }
    }
}
