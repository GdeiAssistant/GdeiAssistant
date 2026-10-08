package cn.gdeiassistant.core.profile.service;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.userprofile.controller.support.ProfileLocationValidator;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.junit.jupiter.api.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.interceptor.*;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfilePatchServiceIntegrationTest {
    private JdbcTemplate db;
    private ProfilePatchService service;
    @BeforeEach void setup() throws Exception {
        var source = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        db = new JdbcTemplate(source);
        db.execute("CREATE TABLE profile(username VARCHAR(16) PRIMARY KEY,nickname VARCHAR(32),birthday DATE,faculty INT,major VARCHAR(80),enrollment INT,location_region VARCHAR(8),location_state VARCHAR(8),location_city VARCHAR(8),hometown_region VARCHAR(8),hometown_state VARCHAR(8),hometown_city VARCHAR(8))");
        db.execute("CREATE TABLE introduction(username VARCHAR(16) PRIMARY KEY,introduction VARCHAR(80) CHECK(introduction <> 'force rollback'))");
        db.update("INSERT INTO profile(username,nickname,faculty,major,birthday) VALUES('alice','before',11,'software_engineering','2000-01-01'),('bob','untouched',11,'software_engineering',null)");
        var config = new XMLConfigBuilder(getClass().getResourceAsStream("/mybatis-config.xml")).parse();
        config.setEnvironment(new Environment("patch", new SpringManagedTransactionFactory(), source));
        config.addMapper(ProfileMapper.class);
        var mapper = new SqlSessionTemplate(new SqlSessionFactoryBuilder().build(config)).getMapper(ProfileMapper.class);
        var certificates = mock(UserCertificateService.class);
        var user = new User(); user.setUsername("alice");
        when(certificates.getUserLoginCertificate("session")).thenReturn(user);
        var target = new ProfilePatchService(mapper, certificates, mock(ProfileLocationValidator.class));
        var interceptor = new TransactionInterceptor(new DataSourceTransactionManager(source), new AnnotationTransactionAttributeSource());
        var proxy = new ProxyFactory(target); proxy.setProxyTargetClass(true); proxy.addAdvice(interceptor);
        service = (ProfilePatchService) proxy.getProxy();
    }
    @Test void commitsBothTablesAndOnlyAuthenticatedUser() throws Exception {
        service.save("session", Map.of("nickname", "after", "introduction", "hello"));
        assertEquals("after", db.queryForObject("SELECT nickname FROM profile WHERE username='alice'", String.class));
        assertEquals("hello", db.queryForObject("SELECT introduction FROM introduction WHERE username='alice'", String.class));
        assertEquals("untouched", db.queryForObject("SELECT nickname FROM profile WHERE username='bob'", String.class));
    }
    @Test void secondWriteFailureRollsBackFirstWrite() {
        assertThrows(Exception.class, () -> service.save("session", Map.of("nickname", "after", "introduction", "force rollback")));
        assertEquals("before", db.queryForObject("SELECT nickname FROM profile WHERE username='alice'", String.class));
        assertEquals(0, db.queryForObject("SELECT count(*) FROM introduction", Integer.class));
    }
    @Test void omittedFieldsStayAndExplicitNullClears() throws Exception {
        service.save("session", Map.of("nickname", "after"));
        assertNotNull(db.queryForObject("SELECT birthday FROM profile WHERE username='alice'", java.sql.Date.class));
        var patch = new HashMap<String,Object>(); patch.put("birthday", null); patch.put("introduction", null);
        service.save("session", patch);
        assertNull(db.queryForObject("SELECT birthday FROM profile WHERE username='alice'", java.sql.Date.class));
        assertEquals("", db.queryForObject("SELECT introduction FROM introduction WHERE username='alice'", String.class));
    }
    @Test void invalidMajorAndFutureBirthdayPreventEveryWrite() {
        var failure = assertThrows(ProfilePatchService.InvalidPatch.class, () -> service.save("session", Map.of("nickname", "after", "major", "not-a-major", "birthday", Map.of("year", 2999, "month", 1, "date", 1))));
        assertTrue(failure.getErrors().containsKey("major")); assertTrue(failure.getErrors().containsKey("birthday"));
        assertEquals("before", db.queryForObject("SELECT nickname FROM profile WHERE username='alice'", String.class));
    }
    @Test void missingIdentityAndInjectedFieldAreRejected() {
        assertThrows(Exception.class, () -> service.save("expired", Map.of("nickname", "after")));
        assertThrows(ProfilePatchService.InvalidPatch.class, () -> service.save("session", Map.of("username", "bob")));
    }
}
