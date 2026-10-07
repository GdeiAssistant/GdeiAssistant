package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler;
import cn.gdeiassistant.core.close.mapper.CloseMapper;
import cn.gdeiassistant.core.deletion.mapper.DeletionCleanupMapper;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.userdata.service.UserDataService;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.userlogin.service.UserLoginService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real MySQL locks and transactions; every external store and school login is synthetic. */
@EnabledIfEnvironmentVariable(named="GDEI_UPGRADE_TEST_JDBC_BASE",matches=".+")
class DeletionLifecycleMySqlTest {
    String schema; JdbcTemplate server,sql; DataSourceTransactionManager transactions;
    DeletionCleanupMapper cleanup; UserMapper users; UserDataService data;
    DeletionCleanupWorker worker; UserLoginService login;
    GradeDao grades; ScheduleDao schedules; UserProfileService profiles; UserCertificateService certificates;
    ExecutorService executor;
    @BeforeEach void setup() throws Exception {
        String base=System.getenv("GDEI_UPGRADE_TEST_JDBC_BASE"),password=System.getenv("GDEI_UPGRADE_TEST_PASSWORD");
        server=new JdbcTemplate(new DriverManagerDataSource(base+"mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true","root",password));
        schema="synthetic_deletion_"+UUID.randomUUID().toString().replace("-","");server.execute("CREATE DATABASE "+schema);
        var source=new DriverManagerDataSource(base+schema+"?sslMode=DISABLED&allowPublicKeyRetrieval=true","root",password);
        sql=new JdbcTemplate(source);transactions=new DataSourceTransactionManager(source);
        sql.execute("CREATE TABLE app_user(id bigint auto_increment primary key,public_id varchar(36),status varchar(16),created_at datetime,updated_at datetime) ENGINE=InnoDB");
        sql.execute("CREATE TABLE campus_credential(user_id bigint primary key,campus_username varchar(24) UNIQUE,password varchar(256)) ENGINE=InnoDB");
        sql.execute("CREATE TABLE account_deletion_cleanup(id varchar(36) primary key,username varchar(24),resetname varchar(24) UNIQUE,user_id bigint,status varchar(16),attempts int default 0,next_attempt_at datetime,locked_until datetime,error_code varchar(100),KEY idx_cleanup_retry(status,next_attempt_at)) ENGINE=InnoDB");
        var factory=new SqlSessionFactoryBean();factory.setDataSource(source);var sessionFactory=factory.getObject();
        sessionFactory.getConfiguration().getTypeHandlerRegistry().register(MybatisEncryptionTypeHandler.class);
        sessionFactory.getConfiguration().addMapper(UserMapper.class);sessionFactory.getConfiguration().addMapper(DeletionCleanupMapper.class);
        var sessions=new SqlSessionTemplate(sessionFactory);users=sessions.getMapper(UserMapper.class);cleanup=sessions.getMapper(DeletionCleanupMapper.class);
        var target=new UserDataService();ReflectionTestUtils.setField(target,"userMapper",users);ReflectionTestUtils.setField(target,"deletionCleanupMapper",cleanup);
        ReflectionTestUtils.setField(target,"profileMapper",mock(ProfileMapper.class));ReflectionTestUtils.setField(target,"privacyMapper",mock(PrivacyMapper.class));
        var proxy=new ProxyFactory(target);proxy.addAdvice(new TransactionInterceptor(transactions,new AnnotationTransactionAttributeSource()));data=(UserDataService)proxy.getProxy();
        certificates=mock(UserCertificateService.class);grades=mock(GradeDao.class);schedules=mock(ScheduleDao.class);profiles=mock(UserProfileService.class);
        worker=new DeletionCleanupWorker();ReflectionTestUtils.setField(worker,"mapper",cleanup);ReflectionTestUtils.setField(worker,"userMapper",users);ReflectionTestUtils.setField(worker,"transactions",transactions);
        ReflectionTestUtils.setField(worker,"gradeDao",grades);ReflectionTestUtils.setField(worker,"scheduleDao",schedules);
        ReflectionTestUtils.setField(worker,"profileService",profiles);ReflectionTestUtils.setField(worker,"certificateService",certificates);
        ReflectionTestUtils.setField(worker,"closeMapper",mock(CloseMapper.class));
        login=new UserLoginService();ReflectionTestUtils.setField(login,"userCertificateService",certificates);ReflectionTestUtils.setField(login,"userDataService",data);
        executor=Executors.newFixedThreadPool(3);
    }
    @AfterEach void close() throws Exception {
        if(executor!=null){executor.shutdownNow();assertTrue(executor.awaitTermination(5,TimeUnit.SECONDS));}
        if(server!=null&&schema!=null)server.execute("DROP DATABASE "+schema);
    }
    void queue(String id,String username) {cleanup.enqueue(id,username,"del_"+id,1L);}
    void register(String username) {try{login.userLogin("synthetic-session",username,"synthetic-password",false);}catch(Exception e){throw new RuntimeException(e);}}
    void assertPending(String username) {
        var failure=assertThrows(RuntimeException.class,()->register(username));
        assertInstanceOf(cn.gdeiassistant.common.exception.AccountCleanupPendingException.class,failure.getCause());
    }
    @Test void pendingCleanupBlocksReRegistrationAndTokenPersistenceUntilAllTasksFinish() throws Exception {
        queue("first","synthetic");queue("second","synthetic");
        assertPending("synthetic");
        assertEquals(0,sql.queryForObject("SELECT COUNT(*) FROM app_user",Integer.class));
        verify(certificates,never()).saveUserLoginCertificate(anyString(),anyString(),anyString());
        worker.runOne("first");assertPending("synthetic");
        worker.runOne("second");register("synthetic");
        assertEquals("ACTIVE",users.selectUser("synthetic").getStatus());
        verify(certificates).saveUserLoginCertificate("synthetic-session","synthetic","synthetic-password");
    }
    @Test void failedExternalCleanupKeepsUsernameReservedAndErrorBodyOutOfQueue() {
        queue("failed","synthetic");doThrow(new IllegalStateException("private external body")).when(grades).removeGrade("synthetic");
        worker.runOne("failed");
        assertEquals("PENDING",sql.queryForObject("SELECT status FROM account_deletion_cleanup",String.class));
        assertEquals("IllegalStateException",sql.queryForObject("SELECT error_code FROM account_deletion_cleanup",String.class));
        assertPending("synthetic");
    }
    @Test void inFlightCleanupHoldsRegistrationUntilLastDeleteAndDoesNotBlockOtherUsername() throws Exception {
        queue("running","synthetic");var entered=new CountDownLatch(1);var release=new CountDownLatch(1);
        doAnswer(call->{entered.countDown();assertTrue(release.await(5,TimeUnit.SECONDS));return null;}).when(grades).removeGrade("synthetic");
        Future<?> cleaning=executor.submit(()->worker.runOne("running"));assertTrue(entered.await(3,TimeUnit.SECONDS));
        Future<?> registering=executor.submit(()->register("synthetic"));
        try {
            assertThrows(TimeoutException.class,()->registering.get(200,TimeUnit.MILLISECONDS));
            executor.submit(()->register("unrelated")).get(2,TimeUnit.SECONDS);
        } finally {release.countDown();}
        cleaning.get(3,TimeUnit.SECONDS);registering.get(3,TimeUnit.SECONDS);
        assertNotNull(users.selectUser("synthetic"));verify(profiles).deleteAvatarForUsername("synthetic");
    }
    @Test void expiredClaimCannotRunTwoDestructiveWorkers() throws Exception {
        queue("expired","synthetic");sql.update("UPDATE account_deletion_cleanup SET status='PROCESSING',locked_until=DATE_SUB(NOW(),INTERVAL 11 MINUTE)");
        var entered=new CountDownLatch(1);var release=new CountDownLatch(1);
        doAnswer(call->{entered.countDown();assertTrue(release.await(5,TimeUnit.SECONDS));return null;}).when(grades).removeGrade("synthetic");
        Future<?> first=executor.submit(()->worker.runOne("expired"));assertTrue(entered.await(3,TimeUnit.SECONDS));
        Future<?> duplicate=executor.submit(()->worker.runOne("expired"));
        try {assertThrows(TimeoutException.class,()->duplicate.get(200,TimeUnit.MILLISECONDS));}finally{release.countDown();}
        first.get(3,TimeUnit.SECONDS);duplicate.get(3,TimeUnit.SECONDS);register("synthetic");worker.runOne("expired");
        verify(grades,times(1)).removeGrade("synthetic");verify(certificates,times(1)).clearReusableCredentials("synthetic");
    }
    @Test void cleanupAfterCommitWritesDurablyInIndependentTransaction() {
        var business=new TransactionTemplate(transactions);
        business.executeWithoutResult(status->{
            queue("committed","synthetic");
            var original=org.springframework.jdbc.datasource.DataSourceUtils.getConnection(sql.getDataSource());
            doAnswer(call->{assertNotSame(original,org.springframework.jdbc.datasource.DataSourceUtils.getConnection(sql.getDataSource()));return null;})
                    .when(grades).removeGrade("synthetic");
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
                @Override public void afterCommit(){worker.afterCommit(new DeletionCleanupWorker.CleanupRequested("committed"));}
            });
        });
        assertEquals("DONE",sql.queryForObject("SELECT status FROM account_deletion_cleanup",String.class));
    }
    @Test void loginRechecksCommittedDeletionAfterWaitingForAccountLock() throws Exception {
        register("synthetic");long oldId=users.selectUser("synthetic").getId();
        var locked=new CountDownLatch(1);var release=new CountDownLatch(1);
        Future<?> deleting=executor.submit(()->new TransactionTemplate(transactions).executeWithoutResult(status->{
            users.selectUserByIdForUpdate(oldId);locked.countDown();
            try{assertTrue(release.await(5,TimeUnit.SECONDS));}catch(InterruptedException e){throw new RuntimeException(e);}
            users.closeAppUser(oldId);users.closeUser("del_waiting","synthetic");queue("waiting","synthetic");
        }));
        assertTrue(locked.await(3,TimeUnit.SECONDS));Future<?> registering=executor.submit(()->register("synthetic"));
        try{assertThrows(TimeoutException.class,()->registering.get(200,TimeUnit.MILLISECONDS));}finally{release.countDown();}
        deleting.get(3,TimeUnit.SECONDS);
        var failure=assertThrows(ExecutionException.class,()->registering.get(3,TimeUnit.SECONDS));
        assertInstanceOf(cn.gdeiassistant.common.exception.AccountCleanupPendingException.class,failure.getCause().getCause());
        assertNull(users.selectUser("synthetic"));assertEquals(1,sql.queryForObject("SELECT COUNT(*) FROM app_user",Integer.class));
    }
    @Test void legacyPendingTaskCannotEraseAnAlreadyReRegisteredAccount() {
        register("synthetic");queue("legacy","synthetic");worker.runOne("legacy");
        verifyNoInteractions(grades,schedules,profiles);verify(certificates,never()).clearReusableCredentials(anyString());
        assertEquals("ACTIVE_USERNAME_CONFLICT",sql.queryForObject("SELECT error_code FROM account_deletion_cleanup",String.class));
        assertEquals("PENDING",sql.queryForObject("SELECT status FROM account_deletion_cleanup",String.class));
        assertNotNull(users.selectUser("synthetic"));
    }
}
