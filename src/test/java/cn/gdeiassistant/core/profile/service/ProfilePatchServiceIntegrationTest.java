package cn.gdeiassistant.core.profile.service;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.userprofile.controller.request.ProfilePatchRequest;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfilePatchServiceIntegrationTest {
    private JdbcTemplate db;
    private ProfilePatchService service;

    @BeforeEach
    void setup() throws Exception {
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
        var user = new User();
        user.setUsername("alice");
        when(certificates.getUserLoginCertificate("session")).thenReturn(user);
        var target = new ProfilePatchService(mapper, certificates, mock(ProfileLocationValidator.class));
        var interceptor = new TransactionInterceptor(new DataSourceTransactionManager(source), new AnnotationTransactionAttributeSource());
        var proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(interceptor);
        service = (ProfilePatchService) proxy.getProxy();
    }

    @Test
    void commitsBothTablesAndOnlyAuthenticatedUser() throws Exception {
        ProfilePatchRequest request = new ProfilePatchRequest();
        request.setNickname("after");
        request.setIntroduction("hello");
        service.save("session", request);
        assertEquals("after", db.queryForObject("SELECT nickname FROM profile WHERE username='alice'", String.class));
        assertEquals("hello", db.queryForObject("SELECT introduction FROM introduction WHERE username='alice'", String.class));
        assertEquals("untouched", db.queryForObject("SELECT nickname FROM profile WHERE username='bob'", String.class));
    }

    @Test
    void secondWriteFailureRollsBackFirstWrite() {
        ProfilePatchRequest request = new ProfilePatchRequest();
        request.setNickname("after");
        request.setIntroduction("force rollback");
        assertThrows(Exception.class, () -> service.save("session", request));
        assertEquals("before", db.queryForObject("SELECT nickname FROM profile WHERE username='alice'", String.class));
        assertEquals(0, db.queryForObject("SELECT count(*) FROM introduction", Integer.class));
    }

    @Test
    void omittedFieldsStayAndExplicitNullClears() throws Exception {
        ProfilePatchRequest first = new ProfilePatchRequest();
        first.setNickname("after");
        service.save("session", first);
        assertNotNull(db.queryForObject("SELECT birthday FROM profile WHERE username='alice'", java.sql.Date.class));

        ProfilePatchRequest clear = new ProfilePatchRequest();
        clear.setBirthday(null);
        clear.setIntroduction(null);
        service.save("session", clear);
        assertNull(db.queryForObject("SELECT birthday FROM profile WHERE username='alice'", java.sql.Date.class));
        assertEquals("", db.queryForObject("SELECT introduction FROM introduction WHERE username='alice'", String.class));
    }

    @Test
    void invalidMajorAndFutureBirthdayPreventEveryWrite() {
        ProfilePatchRequest request = new ProfilePatchRequest();
        request.setNickname("after");
        request.setMajor("not-a-major");
        ProfilePatchRequest.BirthdayPatch future = new ProfilePatchRequest.BirthdayPatch();
        future.setYear(2999);
        future.setMonth(1);
        future.setDate(1);
        request.setBirthday(future);

        var failure = assertThrows(ProfilePatchService.InvalidPatch.class, () -> service.save("session", request));
        assertTrue(failure.getErrors().containsKey("major"));
        assertTrue(failure.getErrors().containsKey("birthday"));
        assertEquals("before", db.queryForObject("SELECT nickname FROM profile WHERE username='alice'", String.class));
    }

    @Test
    void missingIdentityAndInjectedFieldAreRejected() {
        ProfilePatchRequest valid = new ProfilePatchRequest();
        valid.setNickname("after");
        assertThrows(Exception.class, () -> service.save("expired", valid));

        ProfilePatchRequest injected = new ProfilePatchRequest();
        injected.putUnknown("username", "bob");
        var failure = assertThrows(ProfilePatchService.InvalidPatch.class, () -> service.save("session", injected));
        assertTrue(failure.getErrors().containsKey("username"));
    }

    @Test
    void invalidBirthdayNullPartOrPre1900EnrollmentAndOverEscapedNicknameAreRejected() {
        ProfilePatchRequest request = new ProfilePatchRequest();
        request.setNickname("<".repeat(9));
        request.setEnrollment(1899);
        ProfilePatchRequest.BirthdayPatch incomplete = new ProfilePatchRequest.BirthdayPatch();
        incomplete.setYear(2000);
        request.setBirthday(incomplete);

        var failure = assertThrows(ProfilePatchService.InvalidPatch.class, () -> service.save("session", request));
        assertTrue(failure.getErrors().containsKey("nickname"));
        assertTrue(failure.getErrors().containsKey("enrollment"));
        assertTrue(failure.getErrors().containsKey("birthday"));
    }
}
