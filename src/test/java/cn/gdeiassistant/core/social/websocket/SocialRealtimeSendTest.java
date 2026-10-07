package cn.gdeiassistant.core.social.websocket;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.usercertificate.UserCertificateDao;
import cn.gdeiassistant.common.tools.utils.JwtUtil;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import com.auth0.jwt.interfaces.Claim;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SocialRealtimeSendTest {
    SocialRealtimeHub hub;SocialRealtimeHandler handler;WebSocketSession session;Map<String,Object> attrs;
    UserMapper users;UserCertificateDao certificates;JwtUtil jwt;ExecutorService executor;
    @BeforeEach void setup() {
        hub=new SocialRealtimeHub();handler=new SocialRealtimeHandler();session=mock(WebSocketSession.class);attrs=new ConcurrentHashMap<>();
        when(session.getAttributes()).thenReturn(attrs);when(session.isOpen()).thenReturn(true);when(session.getId()).thenReturn("synthetic-ws");
        users=mock(UserMapper.class);certificates=mock(UserCertificateDao.class);jwt=mock(JwtUtil.class);
        ReflectionTestUtils.setField(handler,"realtimeHub",hub);ReflectionTestUtils.setField(handler,"userMapper",users);
        ReflectionTestUtils.setField(handler,"userCertificateDao",certificates);ReflectionTestUtils.setField(handler,"jwtUtil",jwt);
        var account=new CampusAccountView();account.setId(1L);account.setStatus("ACTIVE");
        when(users.selectUser("synthetic")).thenReturn(account);when(users.selectUserById(1L)).thenReturn(account);
        when(certificates.queryUserLoginCertificate("synthetic-session")).thenReturn(new User("synthetic"));
        executor=Executors.newFixedThreadPool(2);
    }
    @AfterEach void close() throws Exception {handler.destroy();executor.shutdownNow();assertTrue(executor.awaitTermination(5,TimeUnit.SECONDS));}
    void authenticated(){attrs.putAll(Map.of("authenticated",true,"userId",1L,"username","synthetic","sessionId","synthetic-session","expiresAt",java.time.Instant.now().getEpochSecond()+300));hub.register(1L,session);}
    Claim claim(String value){var claim=mock(Claim.class);when(claim.asString()).thenReturn(value);return claim;}
    @Test void pongWaitsForInFlightBusinessSendAndNoWritesOverlap() throws Exception {
        authenticated();var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var active=new AtomicInteger();var maximum=new AtomicInteger();
        doAnswer(call->{int concurrent=active.incrementAndGet();maximum.accumulateAndGet(concurrent,Math::max);
            try{if(((TextMessage)call.getArgument(0)).getPayload().contains("business")){entered.countDown();assertTrue(release.await(5,TimeUnit.SECONDS));}}finally{active.decrementAndGet();}return null;}).when(session).sendMessage(any());
        var business=executor.submit(()->hub.pushToUser(1L,Map.of("type","business")));assertTrue(entered.await(3,TimeUnit.SECONDS));
        var started=new CountDownLatch(1);var ping=executor.submit(()->{started.countDown();handler.handleTextMessage(session,new TextMessage("{\"type\":\"ping\"}"));return null;});assertTrue(started.await(3,TimeUnit.SECONDS));
        try{assertThrows(TimeoutException.class,()->ping.get(200,TimeUnit.MILLISECONDS));}finally{release.countDown();}
        business.get(3,TimeUnit.SECONDS);ping.get(3,TimeUnit.SECONDS);assertEquals(1,maximum.get());verify(session,times(2)).sendMessage(any());
    }
    void stubAuth() throws Exception {
        var expiry=mock(Claim.class);when(expiry.asLong()).thenReturn(java.time.Instant.now().getEpochSecond()+300);
        var usernameClaim=claim("synthetic");var sessionClaim=claim("synthetic-session");
        when(jwt.verifyAndParse("synthetic-token")).thenReturn(Map.of("username",usernameClaim,"sessionId",sessionClaim,"exp",expiry));
    }
    @Test void failedReadyRemovesRegistrationAndCancelsRecheck() throws Exception {
        stubAuth();doThrow(new java.io.IOException("synthetic disconnected peer")).when(session).sendMessage(any());
        handler.handleTextMessage(session,new TextMessage("{\"type\":\"auth\",\"token\":\"synthetic-token\"}"));
        assertTrue(((Map<?,?>)ReflectionTestUtils.getField(handler,"sessionRechecks")).isEmpty());
        hub.pushToUser(1L,Map.of("type","business"));verify(session,times(1)).sendMessage(any());verify(session).close();
    }
    @Test void readyAndBusinessPushShareTheSameSendLock() throws Exception {
        stubAuth();
        var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var active=new AtomicInteger();var maximum=new AtomicInteger();var readyLocked=new AtomicBoolean();
        doAnswer(call->{int concurrent=active.incrementAndGet();maximum.accumulateAndGet(concurrent,Math::max);
            try{if(((TextMessage)call.getArgument(0)).getPayload().contains("ready")){readyLocked.set(Thread.holdsLock(session));entered.countDown();assertTrue(release.await(5,TimeUnit.SECONDS));}}finally{active.decrementAndGet();}return null;}).when(session).sendMessage(any());
        var auth=executor.submit(()->{handler.handleTextMessage(session,new TextMessage("{\"type\":\"auth\",\"token\":\"synthetic-token\"}"));return null;});assertTrue(entered.await(3,TimeUnit.SECONDS));
        var started=new CountDownLatch(1);var business=executor.submit(()->{started.countDown();hub.pushToUser(1L,Map.of("type","business"));});assertTrue(started.await(3,TimeUnit.SECONDS));
        try{assertThrows(TimeoutException.class,()->business.get(200,TimeUnit.MILLISECONDS));}finally{release.countDown();}
        auth.get(3,TimeUnit.SECONDS);business.get(3,TimeUnit.SECONDS);assertEquals(1,maximum.get());assertTrue(readyLocked.get(),"ready must hold the same session monitor as business sends");
    }
    @Test void failedPeerCannotChangeCommittedResultOrPreventOtherDeliveryAndIsRemoved() throws Exception {
        authenticated();var working=mock(WebSocketSession.class);when(working.isOpen()).thenReturn(true);when(working.getAttributes()).thenReturn(Map.of("authenticated",true,"userId",1L));hub.register(1L,working);
        doThrow(new IllegalStateException("synthetic transport state")).when(session).sendMessage(any());doThrow(new IllegalStateException("synthetic close state")).when(session).close();
        var source=new DriverManagerDataSource("jdbc:h2:mem:synthetic_ws_"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1","sa","");var sql=new JdbcTemplate(source);
        sql.execute("CREATE TABLE synthetic_event(id int primary key)");var failure=new AtomicReference<RuntimeException>();
        try{
            try{new TransactionTemplate(new DataSourceTransactionManager(source)).executeWithoutResult(status->{sql.update("INSERT INTO synthetic_event VALUES(1)");hub.pushToUserAfterCommit(1L,Map.of("type","business"));});}catch(RuntimeException e){failure.set(e);}
            assertEquals(1,sql.queryForObject("SELECT COUNT(*) FROM synthetic_event",Integer.class));assertNull(failure.get());
            hub.pushToUser(1L,Map.of("type","business"));verify(session,times(1)).sendMessage(any());verify(session).close();verify(working,times(2)).sendMessage(any());
        }finally{sql.execute("DROP ALL OBJECTS");}
    }
}
